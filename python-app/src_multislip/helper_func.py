from datetime import datetime
from io import BytesIO
import shutil
import time
import requests
import logging
import os
import re
import pandas as pd
from typing import Union
from dateutil import parser
logger = logging.getLogger(__name__) 

def error_msg(slip_url, count, original_image_url, image_id,cam_flag):
    transaction_details={}
    transaction_details['Balance']=""
    transaction_details['Bank Account Number']=""
    transaction_details['Bank Name']=""
    transaction_details['Datetime']=""
    transaction_details['DateFormat']=""
    transaction_details['Other Relevant Information']=""
    transaction_details['Reference Number']=""
    transaction_details['Transaction Amount']=""
    transaction_details['Transaction Id']=""
    transaction_details['Transaction Type']=""
    transaction_details['UTR Number']=""
    transaction_details['slip_url']=slip_url
    transaction_details['cam_flag']=cam_flag
    transaction_details['slip_type']=""
    transaction_details['image_count']=count
    transaction_details['original_image_url']=original_image_url
    transaction_details['image_id']=image_id
    transaction_details["error"]=True
    transaction_details['message']="OCR not able to process image"
    return transaction_details

def is_valid(value):
    if isinstance(value, str):
        value = value.lower()
        return value not in ('nan', '', 'na')
    return not pd.isna(value)

def clean_transaction_data(txn_id, utr_no, reference_no, amount):
    try:
        # Ensure all values are strings before processing
        txn_id = str(txn_id or "").strip().replace(" ", "").lstrip("0")
        utr_no = str(utr_no or "").strip().replace(" ", "").lstrip("0")
        reference_no = str(reference_no or "").strip().replace(" ", "").lstrip("0")
        
        # Normalize the amount
        if is_valid(str(amount)):
            # Remove common currency symbols and separators
            amount_cleaned = str(amount).replace("₹", "").replace("¥", "").replace("$", "").replace("€", "").replace("£", "").replace("INR", "").replace(",", "").replace("Rs.", "")
            clean_amount = round(float(amount_cleaned), 2) if amount_cleaned else 0.0
        else:
            clean_amount = 0.0

        account_numbers = {"4184", "6782", "000205034184", "45505426782"}
        # Sanitize account numbers (exact match only)
        if txn_id in account_numbers:
            logger.info(f"Removed txn_id as it is having value  ({txn_id})as account number ")
            txn_id = ""
        if utr_no in account_numbers:
            logger.info(f"Removed utr_no as it is having value  ({utr_no})as account number ")
            utr_no = ""
        if reference_no in account_numbers:
            logger.info(f"Removed reference_no as it is having value  ({reference_no})as account number ")
            reference_no = ""

        logger.info(f"Cleaned Data - txn_id: {txn_id}, utr_no: {utr_no}, reference_no: {reference_no}, amount: {clean_amount}")
        return txn_id, utr_no, reference_no, clean_amount

    except Exception as e:
        logger.error(f"Error in cleaning transaction data: {e}", exc_info=True)
        return txn_id, utr_no, reference_no, clean_amount  

# def result_sanity_check(result_json):
#     required_fields = [
#         "Reference Number",
#         "Transaction Amount",
#         "Transaction Id",
#         "UTR Number",
#         "error",
#         "image_count",
#         "image_id",
#         "message",
#         "original_image_url",
#         "slip_url"
#     ]

#     for txn in result_json:
#         missing_fields = [field for field in required_fields if field not in txn]
#         if missing_fields:
#             txn["error"] = True
#             txn["message"] = "OCR not able to process the image."
#             txn["Reference Number"] = ""
#             txn["Transaction Amount"] = ""
#             txn["Transaction Id"] = ""
#             txn["UTR Number"] = ""
#             txn["Balance"] = ""
#             txn["Bank Account Number"] = ""
#             txn["Bank Name"] = ""
#             txn["Datetime"] = ""
#             txn["Other Relevant Information"] = ""

#     # Filter for required columns to share the result
#     filtered_result = [
#         {field: txn.get(field, "") for field in required_fields}
#         for txn in result_json
#     ]

#     return filtered_result

# def standardize_datetime(dt_string):
#     try:
#         print("Original: ",dt_string)
#         dt = parser.parse(dt_string)  # Auto-detect format
#         print("Converted:",dt.strftime("%Y-%m-%d %H:%M:%S"))
#         return dt.strftime("%Y-%m-%d %H:%M:%S")  # Convert to desired format
#     except Exception as e:
#         logger.error(f"{dt_string}, Issue : {e}")
#         return ""


def remove_google_id(raw_text):
    pattern = r"Google transaction ID\n[^\n]*\n"
    text = re.sub(pattern, "", raw_text)

    # Only remove the word "UPI" (case-insensitive), leave the rest intact
    text = re.sub(r"\bUPI\b", "", text, flags=re.IGNORECASE)
    return text

def standardize_datetime(dt_string):
    try:
        print("Original: ", dt_string)
        dt = datetime.strptime(dt_string, "%d-%m-%Y %H:%M:%S")  # Explicit format parsing
        formatted_dt = dt.strftime("%Y-%m-%d %H:%M:%S")  # Convert to desired format
        print("Converted:", formatted_dt)
        return formatted_dt
    except Exception as e:
        logger.error(f"{dt_string}, Issue: {e}")
        return ""

def result_sanity_check(result_json):
    required_fields = [
        "Reference Number",
        "Transaction Amount",
        "Transaction Id",
        "UTR Number",
        "error",
        "image_count",
        "image_id",
        "message",
        "original_image_url",
        "slip_url",
        "DateFormat",
        "Datetime",
        "cam_flag",
        "slip_type",
    ]
    
    for txn in result_json:
        has_reference = txn.get("Reference Number")
        has_transaction_id = txn.get("Transaction Id")
        has_utr = txn.get("UTR Number")
        has_amount = txn.get("Transaction Amount")


        if (has_reference or has_transaction_id or has_utr) and has_amount:
            if not has_reference:
                txn["Reference Number"] = ""
            if not has_transaction_id:
                txn["Transaction Id"] = ""
            if not has_utr:
                txn["UTR Number"] = ""

            break
        else:
            txn["error"] = True
            txn["message"] = "OCR not able to process the image."
            txn["Reference Number"] = ""
            txn["Transaction Amount"] = ""
            txn["Transaction Id"] = ""
            txn["UTR Number"] = ""
            txn["Balance"] = ""
            txn["Bank Account Number"] = ""
            txn["Bank Name"] = ""
            txn["Datetime"] = ""
            txn["DateFormat"] = ""
            txn["cam_flag"] = txn['cam_flag']
            txn["slip_type"] = ""
            txn["Other Relevant Information"] = ""
    
    for txn in result_json:
        if "Datetime" in txn and txn["Datetime"]:
            txn["Datetime"] = standardize_datetime(txn["Datetime"])
    for txn in result_json:
        if "DateFormat" in txn and txn["DateFormat"]:
            txn["DateFormat"] = standardize_datetime(txn["DateFormat"])
    # Filter for required columns to share the result
    filtered_result = [
        {field: txn.get(field, "") for field in required_fields}
        for txn in result_json
    ]
    
    return filtered_result

def request_download_image(url: str, save_path: str) -> bool:
    try:
        response = requests.get(url)
        logger.info(f"HTTP S3 Download Response: {response}")
        response.raise_for_status()  # Raise error for HTTP codes 4xx/5xx
        with open(save_path, 'wb') as file:
            for chunk in response.iter_content(chunk_size=8192):
                file.write(chunk)
        logger.info("Image downloaded successfully with requests.")
        return True
    except requests.RequestException as e:
        logger.error(f"An HTTP error occurred while downloading the image with requests: {str(e)}")
        return False
    except Exception as e:
        logger.error(f"An error occurred while downloading the image with requests: {str(e)}")
        return False

def get_image_bytes_from_url(url, retries=3, sleep_seconds=2):
    for attempt in range(1, retries + 1):
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            logger.info(f"[Attempt {attempt}] Image fetched successfully from URL.")
            
            img_data = BytesIO(response.content).read()
            return img_data

        except requests.RequestException as e:
            logger.warning(f"[Attempt {attempt}] HTTP error while downloading image: {str(e)}")
        except Exception as e:
            logger.warning(f"[Attempt {attempt}] Unexpected error: {str(e)}")
        
        if attempt < retries:
            logger.info(f"Retrying in {sleep_seconds} seconds...")
            time.sleep(sleep_seconds)

    logger.error("All retry attempts failed. Could not download image.")
    return None
    
# Helper function to clear the temporary file if it exists
def clear_temp_file(file_path: str) -> None:
    try : 
        if os.path.exists(file_path):
            os.remove(file_path)
            logger.info(f"Temporary file {file_path} cleared.")
        else:
            logger.warning(f"Temporary file {file_path} does not exist.")
    except Exception as e: 
        logger.error(f"An error occurred while clearing the temporary file: {str(e)}")
            
def clear_temp_folder(folder_path: str) -> None:
    try : 
        if os.path.exists(folder_path) and os.path.isdir(folder_path):
            shutil.rmtree(folder_path)
            logger.info(f"Temporary folder {folder_path} cleared.")
        else:
            logger.warning(f"Temporary folder {folder_path} does not exist or is not a directory.")
    except Exception as e: 
        logger.error(f"An error occurred while clearing the temporary file: {str(e)}")


def get_filtered_id(data: str) -> str:
    """
    This function extracts a numeric ID from a string that contains the substring 'IMPS'. 
    It removes the 'IMPS' prefix and any additional characters like slashes, hyphens, or 'ICICI', 
    and returns the correct numeric ID. If multiple valid IDs are found, it returns them 
    as a newline-separated string.

    Args:
    - data (str): A string containing a reference to 'IMPS' followed by a numeric ID 
                  and possibly extra characters like slashes, hyphens, or 'ICICI'.

    Returns:
    - str: A formatted string containing the numeric ID(s), separated by newlines if multiple IDs are found.
    
    Example:
    - Input: 'IMPS-12345/ICICIabc'
    - Output: '12345'
    """
    pattern = r'IMPS[-\s*/]?(\d+)(?:[-/ICICI\w]*)?'
    cleaned_data = re.findall(pattern, data)
    # Filter out numbers that are less than 2 digits
    valid_data = [num for num in cleaned_data if len(num) >= 2]
    formatted_string = "\n".join(valid_data)
    return formatted_string


def process_and_update(df_results: pd.DataFrame) -> pd.DataFrame:
    """
    This function processes a DataFrame (`df_results`) and checks three specific columns (`transaction_id`, `utr_number`, `ref_number`) 
    to see if they contain the substring "IMPS". If "IMPS" is found in any of these columns, the corresponding value in that column
    is passed to the `get_filtered_id()` function, which extracts and formats the numeric ID associated with "IMPS". The function 
    then updates the value in the DataFrame with the formatted string returned by `get_filtered_id()`.

    Args:
    - df_results (pd.DataFrame): A pandas DataFrame containing transaction data. 
                                  The columns of interest are `transaction_id`, `utr_number`, and `ref_number`.

    Returns:
    - pd.DataFrame: The updated DataFrame with the formatted values for any row where "IMPS" was found in the relevant columns.
    """
    for index, row in df_results.iterrows():
        # Check if any of the 3 columns (transaction_id, utr_number, ref_number) contain 'IMPS'
        for col in ['Transaction Id', 'UTR Number', 'Reference Number']:
            if col not in df_results.columns:
                logger.warning(f"Column '{col}' not found in DataFrame. Skipping this column.")
                continue
            value = row[col]
            if isinstance(value, str):

                if 'IMPS' in value:  # check if 'IMPS' is in the string
                    # Pass the value to get_filtered_id() and update the value in the dataframe
                    df_results.at[index, col] = get_filtered_id(value)
                elif value.startswith('IDFB'):
                    # Filter out the first 8 characters from the ID
                    df_results.at[index, col] = value[8:]
                elif value.startswith('KARBN'):
                    # Filter in the last 6 characters from the ID
                    df_results.at[index, col] = value[-6:]
                elif value.startswith('TMBLH'):
                    df_results.at[index, col] = value[-8:]
                elif value.startswith('KVBLH'):
                    df_results.at[index, col] = value[-8:]
                
                # Check for special characters
                cleaned_value = df_results.at[index, col].replace(" ", "")
                if re.search(r'[^a-zA-Z0-9]', cleaned_value):
                    df_results.at[index, col] = "Invalid ID"
                    continue
            else:
                logger.warning(f"Value in column '{col}' at index {index} is not a string. Skipping this value.")
    
    return df_results


# def dedupe_transactions_new(transactions):
#     seen_pairs = set()  # (id_value, amount) tuples across all fields
#     deduped_transactions = []

#     def is_valid(value):
#         return value and str(value).strip().lower() not in ('na', 'nan', '', '0', '0.0', '0.00')

#     for transaction in transactions:
#         amount = str(transaction.get("Amount", "")).strip()
#         if not is_valid(amount):
#             amount = ""  # normalize invalid amounts

#         id_values = set()
#         for key in ["Reference Number", "Transaction Id", "UTR Number"]:
#             val = transaction.get(key, "")
#             if is_valid(val):
#                 id_values.add(str(val).strip())

#         # Check if any (id_value, amount) is already seen
#         is_duplicate = any((id_val, amount) in seen_pairs for id_val in id_values)

#         if is_duplicate:
#             continue  # skip if any ID + amount combo is a duplicate

#         # Mark all (id, amount) pairs as seen
#         for id_val in id_values:
#             seen_pairs.add((id_val, amount))

#         deduped_transactions.append(transaction)

#     return deduped_transactions
