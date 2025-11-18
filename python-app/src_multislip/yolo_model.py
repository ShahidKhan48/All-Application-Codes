# import os
# import numpy as np
# import cv2
# from ultralytics import YOLO
# import logging

# logger = logging.getLogger(__name__)

# def yolo_model(img_data):
#     try:
#         logger.info("Starting YOLO model processing.")
        
#         image_arr = np.frombuffer(img_data, dtype=np.uint8)
#         image_arr = cv2.imdecode(image_arr, cv2.IMREAD_COLOR)
#         if image_arr is None or not isinstance(image_arr, (list, tuple)) and not hasattr(image_arr, "shape"):
#             return {"success": False, "message": f"Invalid image array provided. Ensure it's a valid NumPy array.", "result": {}}
#         logger.info("Image array decoded successfully.")

#         try:
#             model = YOLO("src_multislip/best.pt")
#             logger.info("YOLO model loaded successfully.")
#         except Exception as e:
#             logger.error(f"Failed to load YOLO model: {e}")
#             return {"success": False, "message": f"Failed to load YOLO model: {e}", "result": {}}

#         try:
#             results = model(image_arr, iou=0.1)
#             logger.info("YOLO inference completed.")
#         except Exception as e:
#             logger.error(f"Error during YOLO inference: {e}")
#             return {"success": False, "message": f"Error during YOLO inference: {e}", "result": {}}

#         try:
#             slip_count = len(results[0].boxes)
#             slip_type = "multiSlip" if slip_count > 1 else "singleSlip"
#             bboxes = []
#             count = 0
#             for box in results[0].boxes:
#                 count += 1
#                 x1, y1, x2, y2 = map(int, box.xyxy[0]) 
#                 bboxes.append([x1, y1, x2, y2])
#                 cv2.rectangle(image_arr, (x1, y1), (x2, y2), (0, 0, 255), 3)
        
#                 # Add label text (class ID and confidence)
#                 label = f"imageSlip {count}"
#                 cv2.putText(image_arr, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
            
#             logger.info("YOLO results processed successfully.")
#             result = {
#                 "image_data": img_data,
#                 "slip_type": slip_type,
#                 "slip_count": slip_count,
#                 "bbox": bboxes
#             }
#             return {"success": True, "message": "Processing completed successfully", "result": result}
#         except Exception as e:
#             logger.error(f"Error processing YOLO results: {e}")
#             return {"success": False, "message": f"Error processing YOLO results: {e}", "result": {}}

#     except Exception as e:
#         logger.error(f"An unexpected error occurred: {e}")
#         return {"success": False, "message": f"An unexpected error occurred: {e}", "result": {}}



import os
import numpy as np
import cv2
from ultralytics import YOLO
import logging
from datetime import datetime

logger = logging.getLogger(__name__)

def yolo_model(img_data):
    try:
        logger.info("Starting YOLO model processing.")
        
        image_arr = np.frombuffer(img_data, dtype=np.uint8)
        image_arr = cv2.imdecode(image_arr, cv2.IMREAD_COLOR)
        if image_arr is None or not hasattr(image_arr, "shape"):
            return {"success": False, "message": "Invalid image array provided.", "result": {}}
        logger.info("Image array decoded successfully.")

        try:
            model = YOLO("src_multislip/best.pt")
            logger.info("YOLO model loaded successfully.")
        except Exception as e:
            logger.error(f"Failed to load YOLO model: {e}")
            return {"success": False, "message": f"Failed to load YOLO model: {e}", "result": {}}

        try:
            results = model(image_arr, iou=0.1)
            logger.info("YOLO inference completed.")
        except Exception as e:
            logger.error(f"Error during YOLO inference: {e}")
            return {"success": False, "message": f"Error during YOLO inference: {e}", "result": {}}

        try:
            slip_count = len(results[0].boxes)
            slip_type = "multiSlip" if slip_count > 1 else "singleSlip"

            # Extract and sort bounding boxes by x1, then y1
            bboxes = []
            for box in results[0].boxes:
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                bboxes.append([x1, y1, x2, y2])
            bboxes.sort(key=lambda b: (b[0], b[1]))

            # Annotate image with rectangles and labels
            # for idx, (x1, y1, x2, y2) in enumerate(bboxes, start=1):
            #     label = f"imageSlip {idx}"
            #     cv2.rectangle(image_arr, (x1, y1), (x2, y2), (0, 0, 255), 3)
            #     cv2.putText(image_arr, label, (x1, y1 + 10), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

            # # Save the annotated image
            # os.makedirs("data/temp", exist_ok=True)
            # timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")
            # save_path = f"data/temp/annotated_{timestamp}.jpg"
            # cv2.imwrite(save_path, image_arr)
            # logger.info(f"Annotated image saved to {save_path}")

            result = {
                "image_data": img_data,
                "slip_type": slip_type,
                "slip_count": slip_count,
                "bbox": bboxes
            }
            return {"success": True, "message": "Processing completed successfully", "result": result}

        except Exception as e:
            logger.error(f"Error processing YOLO results: {e}")
            return {"success": False, "message": f"Error processing YOLO results: {e}", "result": {}}

    except Exception as e:
        logger.error(f"An unexpected error occurred: {e}")
        return {"success": False, "message": f"An unexpected error occurred: {e}", "result": {}}



# import os
# import numpy as np
# import cv2
# from ultralytics import YOLO
# import logging

# logger = logging.getLogger(__name__)

# def yolo_model(img_data):
#     try:
#         logger.info("Starting YOLO model processing.")
        
#         image_arr = np.frombuffer(img_data, dtype=np.uint8)
#         image_arr = cv2.imdecode(image_arr, cv2.IMREAD_COLOR)
#         if image_arr is None or not hasattr(image_arr, "shape"):
#             return {"success": False, "message": "Invalid image array provided.", "result": {}}
#         logger.info("Image array decoded successfully.")

#         try:
#             model = YOLO("src_multislip/best.pt")
#             logger.info("YOLO model loaded successfully.")
#         except Exception as e:
#             logger.error(f"Failed to load YOLO model: {e}")
#             return {"success": False, "message": f"Failed to load YOLO model: {e}", "result": {}}

#         try:
#             results = model(image_arr, iou=0.1)
#             logger.info("YOLO inference completed.")
#         except Exception as e:
#             logger.error(f"Error during YOLO inference: {e}")
#             return {"success": False, "message": f"Error during YOLO inference: {e}", "result": {}}

#         try:
#             slip_count = len(results[0].boxes)
#             slip_type = "multiSlip" if slip_count > 1 else "singleSlip"

#             # Extract and store bounding boxes
#             bboxes = []
#             for box in results[0].boxes:
#                 x1, y1, x2, y2 = map(int, box.xyxy[0])
#                 bboxes.append([x1, y1, x2, y2])

#             # Sort bounding boxes by x1 then y1
#             bboxes.sort(key=lambda b: (b[0], b[1]))

#             # Optionally, annotate image (debugging or visualization)
#             for idx, (x1, y1, x2, y2) in enumerate(bboxes, start=1):
#                 label = f"imageSlip {idx}"
#                 cv2.rectangle(image_arr, (x1, y1), (x2, y2), (0, 0, 255), 3)
#                 cv2.putText(image_arr, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

#             logger.info("YOLO results processed and sorted successfully.")

#             result = {
#                 "image_data": img_data,
#                 "slip_type": slip_type,
#                 "slip_count": slip_count,
#                 "bbox": bboxes
#             }
#             return {"success": True, "message": "Processing completed successfully", "result": result}

#         except Exception as e:
#             logger.error(f"Error processing YOLO results: {e}")
#             return {"success": False, "message": f"Error processing YOLO results: {e}", "result": {}}

#     except Exception as e:
#         logger.error(f"An unexpected error occurred: {e}")
#         return {"success": False, "message": f"An unexpected error occurred: {e}", "result": {}}
