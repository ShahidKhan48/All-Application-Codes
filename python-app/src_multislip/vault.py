import requests
import json
import os
import yaml
import logging

logger = logging.getLogger(__name__)
def get_vault_secrets():
    vault_url = "https://vault.ninjacart.in/v1/secret/data/ds-service/prod"
    payload = {}
    headers = {
    'X-Vault-Token': os.environ['NC_VAULT_TOKEN']
    }
    keys=["aws_s3_de","azure_computer_vision","azure_openai_params_new"]
    try:
        response = requests.request("GET", vault_url, headers=headers, data=payload)
        secrets = response.text
        secrets = json.loads(secrets)
        secrets = secrets['data']['data']
        if isinstance(secrets,dict):
            for key in keys:
                if key not in secrets.keys():
                    logger.error(f"Missing Credentials : {key}")
                    return None
            logger.info("Credentials Sucessfully Extracted")
            return secrets
        else:
            logger.error("Unexpected JSON structure: 'data' is not a dict")
            return None
    except KeyError as e:
        logger.error(f"Missing key in JSON data:{e}")
        return None
    except json.JSONDecodeError as e:
        logger.error(f"Invalid JSON syntax: {e}")
        return None
    except requests.exceptions.RequestException as e:
        logger.error(f"Request failed: {e}")
        return None
    except Exception as e:
        logger.error(f"Error Occured:{e}")
        return None