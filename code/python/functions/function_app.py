import os
import azure.functions as func
import logging
import uuid
import json
from azure.identity import DefaultAzureCredential
from azure.core.credentials import AzureKeyCredential
from azure.ai.formrecognizer import DocumentAnalysisClient
from azure.storage.blob import BlobServiceClient, BlobClient, ContainerClient

app = func.FunctionApp(http_auth_level=func.AuthLevel.ANONYMOUS)

@app.route(route="saveEmailMetadataInCosmosDB")
@app.queue_output(arg_name="msg", queue_name="outqueue", connection="AzureWebJobsStorage")
@app.cosmos_db_output(arg_name="outputDocument", database_name="DocAIDatabase", 
container_name="EmailExtracts", connection="CosmosDbConnectionString")
def saveEmailMetadataInCosmosDB(
    req: func.HttpRequest,
    msg: func.Out[func.QueueMessage],
    outputDocument: func.Out[func.Document]) -> func.HttpResponse:

    logging.info('Python HTTP trigger function processed a request.')

    messageId = req.params.get('messageId')
    if messageId:
        logging.info(f"Processing message id {messageId}")
    else:
        errorMessage = "Please send the messageId in the HTTP request"
        logging.info(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    req_body = req.get_json()
    if req_body:
        logging.info(f"Request body: {req_body}")
    else:
        # No valid email message content
        errorMessage = "Please send HTTP request body"
        logging.info(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)
    classes = ["none"]
    try:
        messageUri = req_body.get('uri')
        messageType = req_body.get('messageType')        
        receivedTimeFolder = req_body.get('receivedTimeFolder')
        sender = req_body.get('from')
        if messageType == 'email-body':
            url = messageUri + sender + "/" + receivedTimeFolder + "/" + "EmailBody.msg"
            # TODO Classify email message
            try:
                logging.info("Classify email message")
                classes.clear()
                classes.append(f"class[notYetImplemented,0.0];pages[0,0]")
            except Exception as e:
                logging.info(f'Exception raised trying to classify email: {e}')
                # TODO: return with error
        else:
            theAttachmentName = req_body.get('attachmentName')
            if theAttachmentName:
                logging.info(f"Attachment name {theAttachmentName}")                
                url = messageUri + sender + "/" + receivedTimeFolder + "/attachments/" + theAttachmentName
                try:
                    frEndpoint = os.environ['FORM_RECOGNIZER_ENDPOINT']
                    documentClassifierId = os.environ['DOCUMENT_CLASSIFIER_ID']
                    formRecognizerAPIKey = os.environ['FORM_RECOGNIZER_API_KEY']
                    logging.info(f"Document class from [{frEndpoint}] classifier id [{documentClassifierId}]")
                    if formRecognizerAPIKey:
                        # Using API key
                        formRecognizerCredential = AzureKeyCredential(formRecognizerAPIKey)
                    else:
                        # Using Managed Identity
                        formRecognizerCredential = DefaultAzureCredential()                    
                    logging.info("Got Form Recognizer access token")
                    formRecognizerClient = DocumentAnalysisClient(endpoint=frEndpoint,
                                                                credential=formRecognizerCredential)
                    poller = formRecognizerClient.begin_classify_document_from_url(
                                classifier_id=documentClassifierId,
                                document_url=url
                            )
                    
                    classifications = poller.result().documents
                    logging.info(f"Got classifications for attachment from Azure FR: {classifications}")
                    classes.clear()
                    for doc in classifications:
                        identifiedClass = f"class[{doc.doc_type or 'N/A'},{doc.confidence}];pages[{[region.page_number for region in doc.bounding_regions]}]"
                        classes.append(identifiedClass)
                except Exception as e:
                    logging.info(f"Exception raised trying to get classification for attachment {theAttachmentName}: {e}")
                # TODO :
                # - Extract form fields, or key/value using Form Recognizer
                # - Extract data using AOAI
            else:
                logging.info("Attachment name not passed in request body by logic app/client")
    except Exception as e:
        errorMessage = f"When trying to read HttpRequest request body raised exception: {e}"
        logging.info(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)
    
    doc = {
        "id":str(uuid.uuid4()),
        "messageId":messageId,
        "url":url,
        "classes":classes,
        "messageDetails":req_body
    }
    logging.info('Before json dump')
    try:
        inJSON = json.dumps(doc)
    except:
        errorMessage = f"Failed to convert request body to json raising exception: {e}"
        logging.info(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    logging.info(f"Document to store in CosmosDB: {inJSON}")
    outputDocument.set(func.Document.from_json(inJSON))
    msg.set(messageId)
    responseMessage = f"{messageId} data stored in Cosmos DB"
    logging.info(responseMessage)
    return func.HttpResponse(responseMessage, status_code=201)
