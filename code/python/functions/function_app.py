import azure.functions as func
import logging
import uuid
import json

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
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    req_body = req.get_json()
    if req_body:
        logging.info(f"Request body: {req_body}")
    else:
        # No valid email message content
        errorMessage = "Please send HTTP request body"
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    try:
        messageUri = req_body.get('uri')
        messageType = req_body.get('messageType')        
        if messageUri:       
            logging.info(f'messageUri:{messageUri}')
        else:
            # No valid email message content
            errorMessage = f"Please send the messageUri in the HTTP request body."
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
        if messageType:
            logging.info(f'messageType: {messageType}')
        else:
            # No valid email message content
            errorMessage = f"Please send the messageType in the HTTP request body."
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
        receivedTime = req_body.get('receivedTime')
        sender = req_body.get('from')
        if messageType == 'email-body':
            url = messageUri + sender + "/" + receivedTime + "/" + "EmailBody.msg"
        else:
            url = messageUri + sender + "/" + receivedTime + "/attachments/" + req_body.get('attachmentName')
    except ValueError:
        errorMessage = "Raised ValueError when trying to read HttpRequest message body"
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    doc = {
        "id":str(uuid.uuid4()),
        "messageId":messageId,
        "url":url,
        "messageDetails":req_body
    }
    logging.info('Before json dump')
    try:
        inJSON = json.dumps(doc)
    except:
        logging.info("Failed to convert request body to json")
    logging.info(f'Document to store in CosmosDB: {inJSON}')
    outputDocument.set(func.Document.from_json(inJSON))
    msg.set(messageId)
    responseMessage = f'{messageId} data stored in Cosmos DB'
    logging.info(responseMessage)
    return func.HttpResponse(responseMessage, status_code=201)
