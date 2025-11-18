import logging
import boto3
import yaml
from botocore.exceptions import ClientError

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

def load_credentials(yaml_file):
    try:
        with open(yaml_file, 'r') as file:
            credentials = yaml.safe_load(file)
        logger.info("Credentials loaded successfully.")
        return credentials
    except Exception as e:
        logger.error(f"Failed to load credentials from {yaml_file}: {e}")
        raise

# Load credentials
# creds = load_credentials("credentials.yaml")
# AWS_REGION = creds['aws_s3']['region']
# S3_BUCKET = "whatsapp-platform-media"
# s3_client = boto3.client(
#     "s3",
#     aws_access_key_id=creds['aws_s3']['aws_access_key_id'],
#     aws_secret_access_key=creds['aws_s3']['aws_secret_access_key']
# )


def upload_to_s3_get_url(image_dict, image_id,credentials):
    results = []
    
    for idx, (key, image_bytes) in enumerate(image_dict.items(),start=1):
        s3_key = f"clipped_slips/{image_id}_{idx}.jpeg"
        try:
            creds = credentials['aws_s3_de']
            AWS_REGION = creds['region']
            S3_BUCKET = "whatsapp-platform-media"
            s3_client = boto3.client(
                "s3",
                aws_access_key_id=creds['aws_access_key_id'],
                aws_secret_access_key=creds['aws_secret_access_key']
            )
            logger.info(f"Uploading image {s3_key} to S3...")
            s3_client.put_object(Bucket=S3_BUCKET, Key=s3_key, Body=image_bytes, ContentType="image/jpeg")
            image_url = f"https://{S3_BUCKET}.s3.{AWS_REGION}.amazonaws.com/{s3_key}"
            logger.info(f"Successfully uploaded image {s3_key}. URL: {image_url}")
        except ClientError as upload_error:
            error_message = f"Failed to upload image {s3_key}: {str(upload_error)}"
            logger.error(error_message)
            image_url = error_message  # Store the error message instead of URL
        
        # Append tuple (image_bytes, URL or error message)
        results.append((image_bytes, image_url))
    
    return results