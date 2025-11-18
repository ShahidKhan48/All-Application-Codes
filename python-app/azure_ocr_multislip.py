import os
# os.environ['MKL_ENABLE_INSTRUCTIONS'] = 'SSE4.2'
import json
from PIL import Image
import datetime
from dotenv import load_dotenv
import io
import pandas as pd
import regex as re
import numpy as np
import logging
from flask import Flask, request, jsonify
from src_multislip.LLM_data_read import process_receipt_with_azure_gpt3
from src_multislip.ocr_text_extraction import extract_text_from_image
from src_multislip.helper_func import get_image_bytes_from_url,process_and_update, remove_google_id
from src_multislip.yolo_model import yolo_model
from src_multislip.clipper_new import clipper
from src_multislip.helper_func import clean_transaction_data, result_sanity_check, error_msg
from src_multislip.vault import get_vault_secrets
from prometheus_flask_exporter import PrometheusMetrics

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

app = Flask(__name__)
# Enable Prometheus metrics
metrics = PrometheusMetrics(app, path="/metrics")

@app.route('/')
def home():
    return "Hello from Flask app!"

load_dotenv()
secrets=get_vault_secrets()

@app.route("/health", methods=["GET"])
def health_check():
    """
    Health check endpoint to confirm the API is running.
    """
    return jsonify({"status": "healthy", "message": "API is up and running"}), 200

@app.route('/process_transaction', methods=['POST'])
def process_transaction():
    if not secrets:
        logger.error("Vault API Error: Error During API Call")
        jsonify({"error":True, "message":"OCR not able to process the image."}), 400
    img_data=None
    logger.info("##########################################################################")
    logger.info(f"Start of function process_transaction_dev at {datetime.datetime.now()}")
    
    try:
        if request.method == 'POST':
            logger.info("Received POST request for /process_transaction_dev")
            # Parse the incoming JSON request to get the image ID, S3 image link, and clearance sale
            data = request.get_json()
            
            if not data or 'image_url' not in data or 'image_id' not in data :
                logger.error("No image URL, image ID provided in the request.")
                return jsonify({"error":True,"message": "No image URL, image ID, in the request."}), 400
            
            image_url = data['image_url']
            image_id = data['image_id']
            # clearance_amount = data['clearance_amount']
            logger.info(f"Image URL: {image_url}")
            logger.info(f"Image ID: {image_id}")
            # logger.info(f"Clearance Amount: {clearance_amount}")
            try:
                # Download the image from the S3 URL using wget
                # download_success = request_download_image(image_url, save_path)
                img_data = get_image_bytes_from_url(image_url)
                if img_data is None:
                    logger.error("Failed to download image from S3.")
                    return jsonify({"error": True, "message": "OCR not able to process the image."}), 400
            except Exception as e:
                logger.error(f"Image download error: {str(e)}")
                return jsonify({"error":True, "message": "OCR not able to process the image."}), 400
        
        
        #Done Till Here
           
            try:
                # # Open the downloaded image for processing
                # with open(save_path, 'rb') as img_file:
                #     img_data = img_file.read()
                    
                model_response  = yolo_model(img_data)
                
                # Convert image to bytes for OCR processing
                
                
                if not model_response["success"]:
                    logger.error(f"YOLO model error: {model_response['message']}")
                    return jsonify({"error":True, "message": "OCR not able to process the image."}), 400
                
                # Extract details from model response
                image_data = model_response["result"]["image_data"]
                slip_type = model_response["result"]["slip_type"]
                slip_count = model_response["result"]["slip_count"]
                bbox = model_response["result"]["bbox"]
                
                logger.info(f"YOLO model response: slip_type={slip_type}, slip_count={slip_count}, bbox={bbox}")
            
                
                clipper_response = clipper(img_data, bbox, image_id, slip_type, image_url,secrets)
    
                if not clipper_response["success"]:
                    logger.error(f"Clipper error: {clipper_response['message']}")
                    return jsonify({"error":True, "messsage": "OCR not able to process the image."}), 400
                
                image_dict = clipper_response["result"]
                logger.info("Clipper response Success ")
                
                complete_transaction_detail = []
                extracted_texts = []
                for count, (key, (image_bytes, slip_url)) in enumerate(image_dict.items(), start=1):
                    try:
                        img_byte_arr = io.BytesIO(image_bytes)
                        response_extracted_text = extract_text_from_image(img_byte_arr.getvalue(),secrets)
                        if not response_extracted_text["success"]:
                            cam_flag=False
                            transaction_details=error_msg(slip_url, count, image_url, image_id,cam_flag)
                            complete_transaction_detail.append(transaction_details)
                            logger.error(f"OCR extraction error for image {count}: {response_extracted_text['message']}")
                            continue
                            # return jsonify({"error": f"OCR not able to process image {count}."}), 400
                        
                        extracted_text = response_extracted_text['result']
                        logger.info(f"Extracted text for image {count}: {extracted_text}")
                        
                        if not extracted_text:
                            cam_flag=False
                            transaction_details=error_msg(slip_url, count, image_url, image_id,cam_flag)
                            complete_transaction_detail.append(transaction_details)
                            logger.error(f"OCR not able to process image {count}. No text extracted.")
                            continue
                        
                        extracted_text = remove_google_id(extracted_text)
                        raw_ocr_tuple = (extracted_text, slip_url)
                        extracted_texts.append(raw_ocr_tuple)
                    
                    except Exception as e:
                        cam_flag=False
                        transaction_details=error_msg(slip_url, count, image_url, image_id, cam_flag)
                        complete_transaction_detail.append(transaction_details)
                        logger.error(f"OCR processing error for image {count}: {str(e)}")
                        continue 
                    
                if not extracted_texts:
                    return jsonify({"error":True, "message": "OCR not able to process the image."}), 400
                
                logger.info(f"Extracted texts: {extracted_texts}")
                  
            except Exception as e:
                logger.error(f"OCR processing error: {str(e)}")
                return jsonify({"error":True, "message": "OCR not able to process the image."}), 400
            
            try:
                cam_flag = False
                result_json=None
                for count, (text, slip_url) in enumerate(extracted_texts, start=1):
                    try:
                        logger.info(f"Before CamFlag: {cam_flag}")
                        logger.info("Text before camFlag:")
                        logger.info(text)
                        # if 'ATM' in text:
                        if 'ATM' in text and any(keyword in text for keyword in ['CARDHOLDER', 'AGENT NUM', 'TXN NO']):
                            cam_flag = True
                        else:
                            cam_flag = False
                        logger.info(f"CamFlag: {cam_flag}")
                        result = process_receipt_with_azure_gpt3(text,secrets)
                        logger.info(f"Result for text {count}: {result}")
                        if not result["success"]:
                            transaction_details=error_msg(slip_url, count, image_url, image_id,cam_flag)
                            complete_transaction_detail.append(transaction_details)
                            logger.error(f"Processing receipt error for text {count}: {result['message']}")
                            continue  # Skip to the next text

                        transaction_details = json.loads(result["result"])
                        transaction_details["error"]=False
                        transaction_details["message"]="OCR Successfully Processed"
                        transaction_details['slip_url']=slip_url
                        transaction_details['image_count']=count
                        transaction_details['slip_type']=slip_type
                        transaction_details['original_image_url']=image_url
                        transaction_details['image_id']=image_id
                        transaction_details['cam_flag']=cam_flag
                        logger.info(f"Transaction details for text {count}: {transaction_details}")
                        complete_transaction_detail.append(transaction_details)
                    except Exception as e:
                        cam_flag=False
                        transaction_details=error_msg(slip_url, count, image_url, image_id,cam_flag)
                        complete_transaction_detail.append(transaction_details)
                        logger.error(f"Azure GPT-3 processing error for text {count}: {str(e)}")
                        continue 
                try:
                    df = pd.DataFrame(complete_transaction_detail)
                    df = process_and_update(df)
                    print(df)
                    result_json = df.to_dict(orient="records")
                    result_json = result_sanity_check(result_json)
                except Exception as e:
                    logger.error(f"Data processing error: {str(e)}")
                    return jsonify({"error":True , "message": "OCR not able to process the image."}), 400

                if not result_json:
                    return jsonify({"error":True, "message": "OCR not able to process the image."}), 400
                
                for txn in result_json:
                    txn["Transaction Id"], txn["UTR Number"], txn["Reference Number"], txn["Transaction Amount"] = clean_transaction_data(
                        txn.get("Transaction Id"),
                        txn.get("UTR Number"),
                        txn.get("Reference Number"),
                        txn.get("Transaction Amount")
                    )
                return jsonify(result_json), 200
            except Exception as e:
                logger.error(f"Azure GPT-3 processing error: {str(e)}")
                return jsonify({"error":True, "message": "OCR not able to process the image."}), 400
            
            
        
        else:
            logger.error("Method not allowed. Please use POST.")
            return jsonify({"error":True, "message": "Method not allowed. Please use POST."}), 405
    
    except Exception as e:
        # Handle any unexpected errors here
        logger.error(f"An unexpected error occurred: {str(e)}")
        return jsonify({"error":True, "message":"OCR not able to process the image."}), 400
    
        

if __name__ == '__main__':
    # app.run(debug=True)
    app.run(host="0.0.0.0", port=8519, debug=False, use_reloader=False)

