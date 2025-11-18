import numpy as np
import cv2
import os
import logging
from src_multislip.upload_and_get_s3_urls import upload_to_s3_get_url
logger = logging.getLogger(__name__)

def clipper(img_data, bboxes, image_id, slip_type, image_url,credentials):
    image_dict = {}
    try:
        # Decode the binary image data into a NumPy array
        image_arr = np.frombuffer(img_data, dtype=np.uint8)
        image_arr = cv2.imdecode(image_arr, cv2.IMREAD_COLOR)

        if image_arr is None:
            return {"success": False, "message": "Failed to decode the binary image data. Ensure the image is valid.", "result": {}}
        
        logger.info("Image decoded successfully.")

        if slip_type == "multiSlip":
            # Ensure the temp directory exists

            # Iterate over bounding boxes and crop
            for count, bbox in enumerate(bboxes, start=1):
                try:
                    x1, y1, x2, y2 = bbox
                    # Ensure bounding box coordinates are within the image dimensions
                    h, w, _ = image_arr.shape
                    x1, y1, x2, y2 = max(0, x1), max(0, y1), min(w, x2), min(h, y2)
                    
                    if x1 >= x2 or y1 >= y2:
                        logger.warning(f"Invalid bounding box coordinates for bbox {bbox}. Skipping.")
                        continue

                    # Crop the image
                    cropped_img = image_arr[y1:y2, x1:x2]
                    # Encode the cropped image to bytes (e.g., in JPEG format)
                    success, buffer = cv2.imencode(".jpeg", cropped_img)
                    if not success:
                        raise ValueError(f"Failed to encode cropped image for bbox {bbox}.")
                    
                    # Save the bytes in the dictionary
                    image_dict[f"image_{count}"] = buffer.tobytes()
                    
                    # Save the cropped image to the temp directory
                    # cropped_image_path = os.path.join(temp_dir, f"{image_id}_{count}.jpg")
                    # with open(cropped_image_path, 'wb') as f:
                    #     f.write(buffer.tobytes())
                    
                    # logger.info(f"Cropped and saved image for bbox {bbox} successfully as {cropped_image_path}.")

                except Exception as bbox_error:
                    logger.error(f"Error processing bounding box {bbox}: {bbox_error}")
            
            clipper_result = {}
            s3_url_list = upload_to_s3_get_url(image_dict, image_id,credentials)
            if len(s3_url_list)>0:
                for count, item in enumerate(s3_url_list, start=1):
                    clipper_result[f"image_{count}"] = item
                return {"success": True, "message": "Images cropped and saved successfully.", "result": clipper_result}
            else:
                logger.error(f"error Occured while uploading the indivial slips : {e}")
                return{"success": False, "message": f"error Occured while uploading the indivial slips : {e}", "result": {}}
        else:
            # For single slip, return the original image data
            image_dict["image_1"] = (img_data, image_url)
            return {"success": True, "message": "Single image returned successfully.", "result": image_dict}
    
    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}")
        return {"success": False, "message": f"An unexpected error occurred: {e}", "result": {}}