import requests
import yaml
import time
import logging
import os

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

def extract_text_from_image(image_bytes,credentials):
    """Extract text from image using Azure's Computer Vision API"""
    try:
        logger.info("Starting text extraction from image...")
        
        # Load credentials from a file
        # with open("credentials.yaml", 'r') as file:
        #     credentials = yaml.safe_load(file)
        
        azure_cv = credentials.get("azure_computer_vision", {})
        AZURE_CV_ENDPOINT = azure_cv.get("endpoint", "")
        AZURE_CV_KEY = azure_cv.get("api_key", "")
        
        ocr_url = f"{AZURE_CV_ENDPOINT}/vision/v3.2/read/analyze"
        headers = {
            'Ocp-Apim-Subscription-Key': AZURE_CV_KEY,
            'Content-Type': 'application/octet-stream'
        }
        
        logger.info(f"URL: {ocr_url}")
        #removed logger.info for header
        logger.info(f"Image bytes length: {len(image_bytes)}")
        
        # Make the initial request to analyze the image
        response = requests.post(ocr_url, headers=headers, data=image_bytes)
        logger.info(f"Response status code: {response.status_code}")
        logger.info(f"Response content: {response.content}")
        response.raise_for_status()
        
        # Get operation location to poll for results
        operation_url = response.headers.get("Operation-Location", "")
        if not operation_url:
            logger.error("Operation-Location header is missing in the response.")
            return {
                "success": False,
                "message": "OCR Operation-Location header is missing in the response.",
                "result": None
            }
        
        # Poll for the result
        analysis = {}
        poll_delay = 1
        max_retries = 10
        
        for attempt in range(max_retries):
            response_final = requests.get(
                operation_url,
                headers={"Ocp-Apim-Subscription-Key": AZURE_CV_KEY}
            )
            analysis = response_final.json()
            logger.info(f"Polling attempt {attempt + 1}, status: {analysis.get('status', 'unknown')}")
            
            if "status" in analysis and analysis["status"] == "succeeded":
                break
            
            if poll_delay <= 16:
                time.sleep(poll_delay)
                poll_delay *= 2
            else:
                time.sleep(poll_delay)
        
        # Check if the analysis was successful and extract text
        extracted_text = []
        if "analyzeResult" in analysis:
            for read_result in analysis["analyzeResult"]["readResults"]:
                for line in read_result["lines"]:
                    extracted_text.append(line["text"])
        
        logger.info(f"Extracted text: {extracted_text}")
        
        # Return the structured result
        return {
            "success": True,
            "message": "Reading Image Via OCR Successful",
            "result": "\n".join(extracted_text) if extracted_text else None
        }
    
    except requests.exceptions.RequestException as e:
        logger.error(f"OCR API request failed: {str(e)}")
        return {
            "success": False,
            "message": f"OCR API request failed: {str(e)}",
            "result": None
        }
    
    except yaml.YAMLError as e:
        logger.error(f"Error loading credentials: {str(e)}")
        return {
            "success": False,
            "message": f"Error loading credentials: {str(e)}",
            "result": None
        }
    
    except Exception as e:
        logger.error(f"An error occurred: {str(e)}")
        return {
            "success": False,
            "message": f"An error occurred: {str(e)}",
            "result": None
        }