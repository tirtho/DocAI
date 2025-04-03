from azure.identity import DefaultAzureCredential
from azure.core.credentials import AzureKeyCredential
import az_utils
import json
import logging
import shortuuid

# For MI based access to Content Understanding Service from 
# Multi AI Service instance
# we need token for this scope "https://cognitiveservices.azure.com/.default"

def extractVideo(
                     cuEndpoint, 
                     cuAPIKey, 
                     cuAPIVersion,
                     cuDocAIVideoAnalyzerId,
                     videoFileUrl,
                     fName
    ):
    
    fName = f'{fName}f(acu.extractVideo)->'
    cuClientRequestId = f'{shortuuid.uuid()}'

    tokenScope = "https://cognitiveservices.azure.com/.default"
    apiKey = az_utils.getAzureAPIKey(cuAPIKey, tokenScope, fName)
    cuUrl = f'{cuEndpoint}/contentunderstanding/analyzers/{cuDocAIVideoAnalyzerId}:analyze?api-version={cuAPIVersion}'
    headers = {
                "Content-Type": "application/json",
                "Ocp-Apim-Subscription-Key": apiKey,
                "x-ms-client-request-id": cuClientRequestId
    }

    body = {
        "url": f"{videoFileUrl}"
    }
    
    response = az_utils.runHttpRequest(
                endpoint=cuUrl, 
                headers=headers,
                requestType="POST",
                jsonRequestBody=body,
                fName=fName
            )
    
    rReason = response.reason
    responseStatus = response.status_code
    logging.info(f'{fName} Azure Content Understanding Analyze call status: {responseStatus}, \
                    response:{response}')
        
    if responseStatus == 202:
        try:
            jsonResponse = json.loads(response.content.decode('utf-8'))
            cuAnalysisStatus = jsonResponse['status']
            cuReturnedOperationId = jsonResponse['id']
            return responseStatus, cuAnalysisStatus, cuReturnedOperationId, cuClientRequestId
        except Exception as e:
            errorMessage = f'Parsing Azure Content Understanding Analyze API response raised exception:{e}'
            logging.info(f'{fName}errorMessage')
            cuAnalysisStatus = f'Failed: {errorMessage}'
            return responseStatus, cuAnalysisStatus, None, None            
    else:
        cuAnalysisStatus = f'Failed: Azure Content Understanding Analyze API call returned \
            error:{responseStatus}, reason:{rReason}'
        logging.info(f'{fName}cuAnalysisStatus')
        return responseStatus, cuAnalysisStatus, None, None
  