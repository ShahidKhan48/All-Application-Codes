# Multislip CDR Validation

## Overview
**Multislip_CDR_Validation** is an OCR-based project that processes transaction slips, extracts transaction details, and returns structured information in JSON format.

## Folder Structure
```
Multislip_CDR_Validation/
│── azure_ocr_multislip.py  # Main application file
│── requirements.txt        # Dependencies
│
├── src_multislip/          # Supporting scripts
│   ├── best.pt                     # Custom YOLO model
│   ├── clipper_new.py              # Clips multislip using bounding boxes
│   ├── upload_and_get_s3_urls.py   # Uploads clipped images to S3 and returns URLs
│   ├── helper_func.py              # Helper functions for image downloading and OCR data cleaning
│   ├── yolo_model.py               # YOLO model to detect slips and extract bounding boxes
│   ├── ocr_text_extraction.py      # Extracts raw text from images
│   ├── LLM_data_read.py            # extract relevant transaction details
```

## Installation
1. Install dependencies:
   ```sh
   pip install -r requirements.txt
   ```

## Usage
Run the main script:
```sh
python azure_ocr_multislip.py
```

## API Endpoint
### Process Transaction
**Endpoint:**
```
POST http://95.217.8.115:8519/process_transaction
```

**Request Body:**
```json
{
  "image_id" : "123",
  "image_url": "https://s3.ap-south-1.amazonaws.com/ninjacart-test/content/images/collection-report/cash/facilityId_4481_30012025_1738205524939.jpeg"
}
```

**Response Example:**
```json
[
    {
        "Reference Number": "502920363194",
        "Transaction Amount": 5300.0,
        "Transaction Id": "502920363194",
        "UTR Number": "NA",
        "error": false,
        "image_count": 1,
        "image_id": "123",
        "message": "OCR Successfully Processed",
        "original_image_url": "https://s3.ap-south-1.amazonaws.com/ninjacart-test/content/images/collection-report/cash/facilityId_4481_30012025_1738205524939.jpeg",
        "slip_url": "https://whatsapp-platform-media.s3.ap-south-1.amazonaws.com/clipped_slips/123_1.jpeg"
    },
    {
        "Reference Number": "NA",
        "Transaction Amount": 5589.0,
        "Transaction Id": "30401097",
        "UTR Number": "NA",
        "error": false,
        "image_count": 2,
        "image_id": "123",
        "message": "OCR Successfully Processed",
        "original_image_url": "https://s3.ap-south-1.amazonaws.com/ninjacart-test/content/images/collection-report/cash/facilityId_4481_30012025_1738205524939.jpeg",
        "slip_url": "https://whatsapp-platform-media.s3.ap-south-1.amazonaws.com/clipped_slips/123_2.jpeg"
    }
]
```

## Logging
Logs are stored in the `logs/` directory under `application.log`.

