from azure.identity import DefaultAzureCredential
from azure.core.credentials import AzureKeyCredential  
import requests
import json
import logging

def getAzureKeyCredential(api_key, scope_url, fName):
  fName = f'{fName}f(az_utils.getAzureKeyCredential)->'
  try:
    if api_key and not api_key.isspace():
      # the string is non-empty      
      logging.info(f'{fName}Got non-empty API Key')
      # Return credential
      return AzureKeyCredential(api_key)
    else:
      # the string is empty
      try:
        logging.info(f'{fName}Could not get API key from environment variable. Trying Managed ID')
        # Create a ManagedIdentityCredential instance
        credential = DefaultAzureCredential()
        # Use the credential to get a token for a specific scope
        token = credential.get_token(scope_url)
        logging.info(f'{fName}Authenticated successfully with AAD token')
        return AzureKeyCredential(token.token)
      except:
        logging.info(f'{fName}Something went wrong getting token with Managed Identity')
        return
  except:
    logging.info(f'{fName}Something went wrong getting access key')
    return

def getAzureAPIKey(api_key, scope_url, fName):
  fName = f'{fName}f(az_utils.getAzureAPIKey)->'
  try:
    if api_key and not api_key.isspace():
      # the string is non-empty      
      logging.info(f'{fName}Got non-empty API Key')
      return api_key
    else:
      # the string is empty
      try:
        logging.info(f'{fName}Could not get API key from environment variable. Trying Managed ID')
        # Create a ManagedIdentityCredential instance
        credential = DefaultAzureCredential()
        # Use the credential to get a token for a specific scope
        token = credential.get_token(scope_url)
        logging.info(f'{fName}Authenticated successfully with AAD token')
        return token.token
      except:
        logging.info(f'{fName}Something went wrong getting token with Managed Identity')
        return
  except:
    logging.info(f'{fName}Something went wrong getting access key')
    return

def runHttpRequest(endpoint, headers, requestType, jsonRequestBody, fName):
    fName = f'{fName}f(runHttpRequest)->'
    if requestType != 'GET':
        if jsonRequestBody:
            requestBody = json.dumps(jsonRequestBody)
            logging.info(f'{fName}Http Request Body:{requestBody}')
    logging.info(f'{fName}Http Request Headers:{headers}')
    logging.info(f'{fName}Http RequestType:{requestType}; URL:{endpoint}')
    if requestType == 'GET':
        return requests.get(url=endpoint, headers=headers)
    elif requestType == 'PUT':
        return requests.put(url=endpoint, headers=headers, data=requestBody)  
    elif requestType == 'POST':
        return requests.post(url=endpoint, headers=headers, data=requestBody)
    else:
        logging.info(f'{fName} Http method {requestType} not implemented yet')
        return None
