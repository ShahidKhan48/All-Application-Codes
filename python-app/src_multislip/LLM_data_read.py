import os
import yaml
import io
import pandas as pd
# from langchain.chat_models import AzureChatOpenAI
from langchain_community.chat_models import AzureChatOpenAI
from langchain.prompts import ChatPromptTemplate
import json
import logging


#
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

def process_receipt_with_azure_gpt3(extracted_text,credentials):
    try:
        with open("src_multislip/prompt_file.yaml", 'r') as file:
            openai_prompt = yaml.safe_load(file)
        logger.info(f"################### In process_receipt_with_azure_gpt3 {extracted_text}")
        # Load credentials from yaml
        # with open("credentials.yaml", 'r') as file:
        #     credentials = yaml.safe_load(file)
        
        azure_creds = credentials.get("azure_openai_params_new", {})
        os.environ["AZURE_OPENAI_API_KEY"] = azure_creds.get("AZURE_OPENAI_API_KEY", "")
        os.environ["AZURE_OPENAI_ENDPOINT"] = azure_creds.get("AZURE_OPENAI_ENDPOINT", "")
        os.environ["AZURE_OPENAI_API_VERSION"] = azure_creds.get("AZURE_OPENAI_API_VERSION", "")
        os.environ["AZURE_OPENAI_CHAT_DEPLOYMENT_NAME"] = azure_creds.get("AZURE_OPENAI_CHAT_DEPLOYMENT_NAME", "")
        
        # receipt_llm = AzureChatOpenAI(
        #     model="gpt-35-turbo",
        #     deployment_id = "chat",
        #     openai_api_version="2024-06-01",
        #     temperature=0
        # )
        
        receipt_llm = AzureChatOpenAI(
            model="gpt-35-turbo",
            openai_api_version=os.getenv("AZURE_OPENAI_API_VERSION", ""),
            temperature=0
        )

        prompt = ChatPromptTemplate.from_messages([
            ("system", openai_prompt['openai-prompt']),
            ("user", """Here is the raw text extracted from the receipt:

            {extracted_text}

            Please extract and structure the data as described.""")
        ])
        # prompt = ChatPromptTemplate.from_messages([
        #     ("system", """You are an assistant that extracts structured information from text found in bank receipts.

        #     Your task is to:
        #     - Identify the Bank Name AS "Bank Name"
        #     - Identify the Bank Account Number to which the payment is made, including UPI IDs. Any payment made to "ninjacart" or "63Ideas Infolabs private limited" associated account numbers or UPI IDs like ("ninjacart1234@bank") should be identified AS "Bank Account Number". If the Bank Account Number is found, extract only the last 4 digits of the account number and return that. 
        #       Don't identify the Bank Account Number from  which the payment is done, Identify the receiver's Bank Account Number only.
        #       Example: For a UPI ID like "ninjacart1234@bank", the bank account number is "1234".'
              
        #     - Extract the Transaction Amount AS "Transaction Amount"
        #     - Extract the Transaction Id  AS "Transaction Id"
        #     - Extract the Reference Number or RRN Number AS "Reference Number"
        #     - Extract the UTR Number AS "UTR Number"
        #     - Extract the Date and Time of Transaction AS "Datetime" in format '%Y-%m-%d %H:%M:%S'
        #     - Identify the Transaction Type (e.g., Deposit, Withdrawal) AS "Transaction Type"

        #     - Extract Any Other Relevant Information AS "Other Relevant Information".
        #     Format the output as a JSON object always. 
        #     Keep the output in fixed format don't return the messages in chat format response.
        #     If any of these are missing, return 'NA' for that field. Ensure no data is fabricated or assumed.
        #     """),
        #     ("user", """Here is the raw text extracted from the receipt:

        #     {extracted_text}

        #     Please extract and structure the data as described.""")
        # ])
            # - Extract the Total Balance Amount of the Account AS "Balance"

        logger.info(f"################### {extracted_text}")
        context = {"extracted_text": extracted_text}
        
        prompt_text = prompt.format_messages(extracted_text=extracted_text)
        logger.info(prompt_text)
        try:
            response = receipt_llm(prompt_text)
            logger.info(f"Response from Azure GPT-3: {response}")
            structured_data = response.content
            
            if structured_data.startswith("```json"):
                structured_data = structured_data[7:-3].strip()

            # if isinstance(structured_data, str):
            #     structured_data = json.loads(structured_data)
            
            return {"success": True, "message": "Text processed successfully", "result": structured_data}
        except Exception as e:
            return {"success": False, "message": f"Error processing the text with Azure GPT: {str(e)}", "result": None}
        
    except Exception as e:
        return {"success": False, "message": f"Error processing the text with Azure GPT: {str(e)}", "result": None}

       
        