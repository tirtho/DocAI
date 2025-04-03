import logging
import uuid
import json
import os
import datetime
import ast
import base64

import azure.functions as func
import aoai
import docintel
import acu

from azure.identity import DefaultAzureCredential
from azure.core.credentials import AzureKeyCredential
from azure.storage.blob import BlobServiceClient, BlobClient, ContainerClient

app = func.FunctionApp(http_auth_level=func.AuthLevel.ANONYMOUS)
def getCurrentUTCTimeString():
    # Get the current date and time
    current_time = datetime.datetime.now()
    # Format the date and time as yyyy-mm-ddTHH:MM:SS
    return current_time.strftime('%Y-%m-%dT%H:%M:%S.%f')

    print(formatted_time)
    
def getHttpRequestBody(request, functionName):
    fName = f'{functionName}f(getHttpRequestBody)->'
    logging.info(f'{fName}Received HTTP request from Azure HTTP triggered Function')
    try:
        messageId = request.params.get('messageId')
    except Exception as e:
        errorMessage = f"{fName}Error: Trying to get messageId got Exception:{e}"
        logging.error(errorMessage)
        raise   
    if messageId:
        logging.info(f"{fName}messageId:{messageId}")
    else:
        errorMessage = f"{fName}Error: messageId not found in HTTP request"
        logging.error(errorMessage)
        raise ValueError(errorMessage)   
    try:
        theRequestBodyJson = request.get_json()
    except Exception as e:
        errorMessage = f"{fName}Error: Trying to get request body got Exception:{e}"
        logging.error(errorMessage)
        raise
    if theRequestBodyJson:
        logging.info(f'{fName}Request body:{theRequestBodyJson}')
    else:
        # No valid email message content
        errorMessage = f'{fName}Error: Request body not found in HTTP request'
        logging.error(errorMessage)
        raise ValueError(errorMessage)
    return messageId, theRequestBodyJson

def getItemFromRequestBody(requestBody, itemName, fName):
    fName = f'{fName}f(getItemFromRequestBody)->'
    try:
        item = ""
        item = requestBody.get(itemName)
        logging.info(f'{fName}From http request body found {itemName}={item}')
        return item
    except Exception as e:
        errorMessage = f'{fName}Error: {itemName} not found in request body'
        logging.error(errorMessage)
        raise

def composePromptWithRAGData(body, fName):
    fName = f'{fName}f(composePromptWithRAGData)->'
    logging.info(f'{fName}Composing prompt')
    ragArray = []
    
    # TODO: Replace hard coded data with data from CogSearch
    resultFromSearch = {
        "body": "Hi,\n\nI am interested in obtaining an auto insurance quote. I own a 2015 Toyota Camry, VIN number DJ6VGIH287R2598 with license plate ZPQ-1763. Despite having three previous claims and approximately 65,000 miles of travel under its belt, the vehicle is still in good condition.\n\nAlongside me as primary driver are my wife Sarah Thompson and our daughter Emily Thompson who often share driving responsibilities too – both have clean records devoid of any accidents or violations at fault over recent years.\n\nCould you provide information on suitable plans considering these parameters?\n\nThank you,\n\nMark Thompson",
        "categories": "auto-insurance,request-new-quote"
    }   
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi,\n\nOn July 17th, 2021 at approximately 7:45pm I was involved in an accident with my Toyota Corolla on Maple Drive, Kansas. The incident occurred when a deer suddenly crossed the road causing me to swerve and collide into a tree. Myself (John Doe), Anna Schmidt, Hiroshi Tanaka and Aman Gupta were all present during this unfortunate event.\n\nThe impact caused significant damage to the front bumper and left side mirror of my vehicle. Additionally, I suffered from minor whiplash due to sudden braking before hitting the tree. Please contact me back regarding claim procedures at (123)456-7890.\n\nThank you,\n\nJohn",
        "categories": "auto-insurance,request-new-claim"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Dear TR Insurance,\n\nMy name is Jacob Mitchell and I am interested in acquiring an auto insurance quote. The vehicle of interest is a 2004 Toyota Corolla that has covered approximately 95,000 miles. Despite having four previous claims on record, the car remains to be in good condition.\n\nIn addition to me as the principal driver, please extend your coverage options inclusive of my wife Laura and daughter Emma who also drive this car from time-to-time. We all have clean driving records.\n\nI would appreciate it if you could provide comprehensive information related to various plans available along with their associated costs at your earliest convenience so we can move forward swiftly.\n\nThank You,\n\nJacob Mitchell",
        "categories": "auto-insurance,request-new-quote"
    }   
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Dear Sir/Madam,\n\nI, Keiji Suzuki, was involved in an accident with my Ford Mustang on August 15th, 2022 at approximately 7.30pm along Elm Street in Texas when a vehicle abruptly changed lanes and collided into mine. The individuals included are myself (Keiji Suzuki), Li Wei Zhang, Aditi Sharma and Kwame Nkrumah. My car's rear bumper suffered significant damage while I sustained minor bruises to the forearm.\n\nPlease process the claim as soon as possible\n\nThanks,\n\nKeiji Suzuki",
        "categories": "auto-insurance,request-new-claim"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi James,\n\nPlease find enclosed the Commercial Insurance application form duly filled in and signed.\n\nPlease process the information and send me a quote for insurance, including monthly premiums, coverage details etc.\n\nThanks,\n\nKanchan Roy",
        "categories": "commercial-insurance,request-new-quote"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi JK, On behalf of New England Commercial Construction Company, I am enclosing the Commercial Insurance ACORD form. They need an insurance quote for their business by end of the week. Please call me or email me if you have any questions.\n\nRegards,\n\n- Nathaniel",
        "categories": "commercial-insurance,request-new-quote"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi Ram, Hope all is well at your end. Since we last met, I have worked on filling up the worker compensation application form for my organization. As you are aware, we are a rapidly growing startup. Our employee headcount is also increasing in recently months. So, we need worker compensation coverage for our employees as soon as possible, to mitigate associated risks. I am attaching the form here. Please respond back ASAP.\n\nRegards,\n\n- Roger",
        "categories": "workers-compensation,request-new-quote"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi, Please find attached our workers compensation application. Please respond with a quote, as discussed.\n\nRegards,\n\n- Hari",
        "categories": "workers-compensation,request-new-quote"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi Sam, Please find enclosed my medical records. Based on this information can you please provide a quote for Life Insurance?.\n\nRegards,\n\n- JK",
        "categories": "life-insurance,request-new-quote"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi Erin, I have attached my blood test report and all the recent prescriptions. Please provide a life insurance quote, as discussed.\n\nRegards,\n\n- Akashi",
        "categories": "life-insurance,request-new-quote"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi, As discussed here are all my recent health records for you to peruse and provide a life insurance quote. Thanks!",
        "categories": "life-insurance,request-new-quote"
    }
    ragArray.append(resultFromSearch)
    resultFromSearch = {
        "body": "Hi Dr House, Here are all the prescriptions and lab reports. Please email me back a life insurance quote.\n\nRegards,\n\n- Iqbal",
        "categories": "life-insurance,request-new-quote"
    }
    ragArray.append(resultFromSearch)
    
    allShots = ''
    allCategoriesArray = ['unknown']
    i = 0
    for aRAG in ragArray:
        i = i + 1
        aShot = f'\nEmail {i}: {aRAG["body"]}\nCategory: {aRAG["categories"]}'
        allShots = allShots + aShot
        theCategoriesString = aRAG["categories"]
        # RAG might return for categories something like "auto-insurance, request-new-claim"
        # Now lets loop thru each of these categories returned
        # find if it already exists in allCategoriesArray
        # and if not then add it to the array
        if theCategoriesString:
            cArray = [x.strip() for x in theCategoriesString.split(',')]
        if cArray:
            for c in cArray:
                if c not in allCategoriesArray:
                    allCategoriesArray.append(c)
    allCategoriesString = ','.join(allCategoriesArray)  
    thePrompt = [
                    {
                    "role": "user", 
                    "content": f"Classify the following email from the following categories: \n{allCategoriesString}\n{allShots}\nEmail {i+1}: {body}\nCategory: "
                    }
                ]       
    return thePrompt
    
def getEmailClassesFromOpenAI(subject, body, fName):
    fName = f'{fName}f(getEmailClassesFromOpenAI)->'
    logging.info(f'{fName}Calling OpenAI to get email classes')
    categories = [
                {
                    "category": "unknown"
                }
              ]   
    try:
        cliEngine = os.getenv('DOCAI_EMAIL_CLASSIFIER_ENGINE')
        aoai_status, aoai_client = aoai.setupOpenai(
                                            os.getenv('DOCAI_EMAIL_CLASSIFIER_API_ENDPOINT'),
                                            os.getenv('DOCAI_EMAIL_CLASSIFIER_API_KEY'),
                                            os.getenv('DOCAI_EMAIL_CLASSIFIER_API_VERSION'))
        if aoai_status == True:
            logging.info(f'{fName}OpenAI connection setup successful for email classifier')
            gotPrompt = composePromptWithRAGData(body,fName)
            logging.info(f'{fName}Got Prompt: {gotPrompt}')
            tokens_used, finish_reason, classifiedCategories = aoai.getChatCompletion(
                                                                the_client=aoai_client,
                                                                the_model=cliEngine, 
                                                                the_messages=gotPrompt)
            if classifiedCategories:
                categories.clear()
                classifiedCategoriesArray = [x.strip() for x in classifiedCategories.split(',')]
                for aCategory in classifiedCategoriesArray:
                    aClassInfo = {
                        "category":aCategory
                    }
                    categories.append(aClassInfo)
        else:
            errorMessage = f'{fName}ERROR: OpenAI connection setup raised exception:{e}'
            logging.error(errorMessage)
            raise ValueError(errorMessage)
    except Exception as e:
        errorMessage = f'{fName}ERROR: OpenAI for email classification raised exception:{e}'
        logging.error(errorMessage)
        raise
    logging.info(f'{fName}Categories:{categories}')
    return categories

def getAttachmentClassesFromFormRecognizer(attachmentUrl, classifierId, fName):
    fName = f'{fName}getAttachmentClassesFromFormRecognizer->'
    logging.info(f'{fName}Calling Document Intelligence to get attachment categories')
    confidenceThreshold = float(os.getenv('DOCAI_DOCUMENT_CONFIDENCE_THRESHOLD'))
    logging.info(f'{fName}Confidence threashold DOCAI_DOCUMENT_CONFIDENCE_THRESHOLD = {confidenceThreshold}')
    try:
        formRecognizerEndpoint = os.getenv('DOCAI_DOCINTEL_API_ENDPOINT')
        logging.info(f'{fName}Starting Azure Document Intelligence Service connection with {formRecognizerEndpoint}')
        formRecognizerApiKey = os.getenv('DOCAI_DOCINTEL_API_KEY')
        formRecognizerCredential = docintel.getDocumentIntelligenceCredential(formRecognizerApiKey)
        formRecognizerClient = docintel.getDocumentIntelligenceClient(
                                        endpoint=formRecognizerEndpoint,
                                        credential=formRecognizerCredential
                                    )
        logging.info(f'{fName}Getting categories for attachment {attachmentUrl}')
        result = docintel.classifyOnlineDocument(
                        client=formRecognizerClient,
                        model=classifierId,
                        file_url=attachmentUrl
                    )
        categories = docintel.getCategories(result, confidenceThreshold)
    except Exception as e:
        errorMessage = f'{fName}ERROR:Get classes for {attachmentUrl} raised exception: {e}'
        logging.error(errorMessage)
        raise

    logging.info(f'{fName}Categories:{categories}')
    return categories

# If the categories list has a category that contains theCategory string in it
def containsCategory(categories, theCategory, fName):
    fName = f'{fName}f(hasCategory)->'
    try:
        # Assuming classes was passed as a string
        categoriesMap = ast.literal_eval(categories)
    except:
        # Nope, it was passed as a list
        categoriesMap = categories   
    try:
        for aCategory in categoriesMap:
            cat = aCategory['category']
            if theCategory in cat:
                return True
    except Exception as e:
        logging.info(f'{fName}Finding matching category {theCategory} in categories {categories} raised exception:{e}')
    return False    

def getDocumentExtractionModelFromClasses(categories, fName):
    fName = f'{fName}f(getDocumentExtractionModelFromClasses)->'
    logging.info(f'{fName}Retrieving Form Recognizer Extraction Model Id from class')
    highestConfidence = 0
    documentCategory = "unknown"
    try:
        # Assuming classes was passed as a string
        categoriesMap = ast.literal_eval(categories)
    except:
        # Nope, it was passed as a list
        categoriesMap = categories   
    for aCategory in categoriesMap:
        logging.info(f'{fName}Category sent by caller:{aCategory}')
        try:
            thisConfidence = aCategory['confidence']
            if thisConfidence:
                if thisConfidence > highestConfidence:
                    highestConfidence = thisConfidence
                    documentCategory = aCategory['category']
                    logging.info(f'{fName}Found category from environment that matches category passed by caller:category{documentCategory};confidence{thisConfidence}')
        except:
            #skip, as there is no confidence found
            continue
    modelClassMapFromEnvironment = f'{os.getenv("DOCAI_DOCINTEL_EXTRACTION_MODEL_CLASS_MAP")}'
    modelClassMap = ast.literal_eval(modelClassMapFromEnvironment)
    logging.info(f'{fName}Model Map from Environment variable DOCAI_DOCINTEL_EXTRACTION_MODEL_CLASS_MAP is {modelClassMap}')
    formRecognizerExtractionModel = None    
    for aModelClass in modelClassMap:
        logging.info(f'{fName}Model Map Class:{aModelClass}')
        try:
            if aModelClass[documentCategory]:
                formRecognizerExtractionModel = aModelClass[documentCategory]
                break
        except:
            # Skip nothing to do
            continue
    if formRecognizerExtractionModel == None:
        errorMessage = f'{fName}Form Recognizer Extraction model for category {documentCategory} not found in DOCUMENT_EXTRACTION_MODEL_CLASS_MAP, hence selecting unknown for model'
        logging.error(errorMessage)
        raise ValueError(errorMessage)
    if formRecognizerExtractionModel != "unknown":
        theModelType = "custom-model"
    else:
        theModelType = "unknown-model"

    logging.info(f'{fName}Retrieved model type:{theModelType}, model:{formRecognizerExtractionModel} from category:{documentCategory} with highest confidence:{highestConfidence}')
    return theModelType, formRecognizerExtractionModel    
    
def composeMultiModalPrompt(url, fName):
    fName = f'{fName}f(composeMultiModalPrompt)->'
    return [ 
            { 
                "role": "system", 
                "content": "You are a helpful assistant." 
            }, 
            { 
                "role": "user", 
                "content": [  
                    { 
                    "type": "text", 
                    "text": "Classify this picture in one word ONLY from one of the classes 'automobile', 'home', 'medical-rx-note', 'medical-lab-report', 'financial-report', 'other':" 
                    },
                    { 
                    "type": "image_url",
                    "image_url": {"url": url}
                    }
                ]
            } 
        ]

def composeImageExtractionPrompt(url, categories, fName):
    fName = f'{fName}f(composeImageExtractionPrompt)->'
    return [ 
            { "role": "system", "content": "You are a helpful assistant." }, 
            { "role": "user", "content": [  
                { 
                    "type": "text", 
                    "text": "Describe image in less than 500 words:" 
                },
                { 
                    "type": "image_url",
                    "image_url": {"url": url}
                }
              ]
            } 
        ]

def hasHandwrittenTextPrompt(url, fName):
    fName = f'{fName}f(hasHandwrittenTextPrompt)->'
    return [ 
            { "role": "system", "content": "You are a helpful assistant." }, 
            { "role": "user", "content": [  
                { 
                    "type": "text", 
                    "text": "Is there any hand written text in this document? Answer just True or False." 
                },
                { 
                    "type": "image_url",
                    "image_url": {
                        "url": url
                    }
                }
            ] } 
        ]

def composeOmniExtractionPrompt(url, fName):
    fName = f'{fName}f(composeOmniExtractionPrompt)->'
    return [ 
            { "role": "system", "content": "You are a helpful assistant." }, 
            { "role": "user", "content": [  
                { 
                    "type": "text", 
                    "text": "Describe image in less than 10 sentences:" 
                },
                { 
                    "type": "image_url",
                    "image_url": {
                        "url": url
                    }
                }
            ] } 
        ]
    
def getExtractsFromImage(url, categories, fName):
    fName = f'{fName}f(getExtractsFromImage)->'
    aoaiMultiModalAPIKey = os.getenv('DOCAI_IMAGE_API_KEY')
    aoaiMultiModalAPIEndpoint = os.getenv('DOCAI_IMAGE_API_ENDPOINT')
    aoaiOmniAPIVersion = os.getenv('DOCAI_IMAGE_API_VERSION')
    aoaiOmniAPIEngine = os.getenv('DOCAI_IMAGE_API_ENGINE')
    try:
        aoai_status, aoai_client = aoai.setupOpenai(
                                            aoaiMultiModalAPIEndpoint,
                                            aoaiMultiModalAPIKey,
                                            aoaiOmniAPIVersion
                                        )
        if aoai_status == True:
            logging.info(f'{fName}OpenAI connection setup successful')
            rawToken = str(os.getenv('DOCAI_BLOB_STORE_SAS_TOKEN'))
            decodedBytes = base64.b64decode(rawToken)
            blobStoreSASToken = decodedBytes.decode("utf-8")[2:-2]
            logging.info(f'{fName}Blob SAS token:{blobStoreSASToken}')
        
            # Extract summary from image
            gotPrompt = composeOmniExtractionPrompt(f'{url}?{blobStoreSASToken}', fName)
            logging.info(f'{fName}Got Prompt for summary: {gotPrompt}')
            
            tokens_used, finish_reason, completion = aoai.getChatCompletion(
                                                                the_client=aoai_client,
                                                                the_model=aoaiOmniAPIEngine, 
                                                                the_messages=gotPrompt)
            logging.info(f'{fName} GPT4o API Completion succeeded:tokens used:{tokens_used}')
            summary = completion
            
            # Detect handwritten text in image
            gotPrompt = hasHandwrittenTextPrompt(f'{url}?{blobStoreSASToken}', fName)
            logging.info(f'{fName}Got Prompt to detect handwritten text in image: {gotPrompt}')
            tokens_used, finish_reason, completion = aoai.getChatCompletion(
                                                                the_client=aoai_client,
                                                                the_model=aoaiOmniAPIEngine, 
                                                                the_messages=gotPrompt)
            logging.info(f'{fName} GPT4o API Completion succeeded:tokens used:{tokens_used}')            
            isHandwritten = completion
        else:
            errorMessage = f'{fName}ERROR: OpenAI connection setup raised exception:{e}'
            logging.error(errorMessage)
            raise ValueError(errorMessage)
    except Exception as e:
        errorMessage = f'{fName}ERROR: OpenAI raised exception:{e}'
        logging.error(errorMessage)
        raise    

    aoaiAPIVersion = aoaiOmniAPIVersion
    modelId = aoaiOmniAPIEngine
    formDocuments = []
    formFields = []
    name = "Description"
    value_type = "string"
    # TODO: Compute confidence for what is returned by GPT4
    #    "fieldConfidence":confidence,
    aField = {
        "fieldName":f'{name}',
        "fieldValueType":f'{value_type}',
        "fieldValue":f'{summary}'
    }
    formFields.append(aField)
    createdDateTime = getCurrentUTCTimeString()
    aField = {
        "fieldName":"CreatedTime",
        "fieldValueType":"string",
        "fieldConfidence":0.99,
        "fieldValue":f'{createdDateTime}'
    }
    formFields.append(aField)
    # TODO: compute confidence for doc
    # "documentConfidence":confidence,
    aDocument = {
        "documentId":0,
        "documentType":f'{aoaiOmniAPIEngine}',
        "fields":formFields
    }
    formDocuments.append(aDocument)
    logging.info(f'{fName}formDocuments:{formDocuments}')
    return aoaiAPIVersion, modelId, isHandwritten, formDocuments  

# You call Content Understanding Service to analyze the video
# Unlike image files, you do not get much extracted in this call
# Once extraction is complete (takes many minutes, depending on size of view)
# you get the extraction as part of the "Review Summary" from 
# the web UI 
def getExtractsFromVideo(url, fName):
    fName = f'{fName}f(getExtractsFromVideo)->'
    cuAPIVersion = os.getenv('DOCAI_CU_API_VERSION')
    rawToken = str(os.getenv('DOCAI_BLOB_STORE_SAS_TOKEN'))
    decodedBytes = base64.b64decode(rawToken)
    blobStoreSASToken = decodedBytes.decode("utf-8")[2:-2]
    logging.info(f'{fName}Blob SAS token:{blobStoreSASToken}')        
    videoFileUrl = f'{url}?{blobStoreSASToken}'
    cuEndpoint = os.getenv('DOCAI_CU_API_ENDPOINT')
    cuAPIKey = os.getenv('DOCAI_CU_API_KEY')
    cuDocAIVideoAnalyzerId = os.getenv('DOCAI_CU_VIDEO_ANALYZER_ID')
    createdDateTime = getCurrentUTCTimeString()

    responseStatus, cuAnalysisStatus, cuReturnedOperationId, cuRequestId = acu.extractVideo(
                     cuEndpoint, 
                     cuAPIKey,
                     cuAPIVersion,
                     cuDocAIVideoAnalyzerId, 
                     videoFileUrl,
                     fName
                    )
    
    modelId = f'video-{cuDocAIVideoAnalyzerId}'
    isHandwritten = False
    formDocuments = []
    formFields = []
    aField = {
        "fieldName":"AnalyzerId",
        "fieldValueType":"string",
        "fieldConfidence":0.99,
        "fieldValue":f'{cuDocAIVideoAnalyzerId}'
    }
    formFields.append(aField)
    aField = {
        "fieldName":"OperationId",
        "fieldValueType":"string",
        "fieldConfidence":0.99,
        "fieldValue":f'{cuReturnedOperationId}'
    }
    formFields.append(aField)
    aField = {
        "fieldName":"IngestionStatus",
        "fieldValueType":"string",
        "fieldConfidence":0.99,
        "fieldValue":f'{cuAnalysisStatus}'
    }
    formFields.append(aField)
    aField = {
        "fieldName":"ContentUnderstandingApiVersion",
        "fieldValueType":"string",
        "fieldConfidence":0.99,
        "fieldValue":f'{cuAPIVersion}'
    }
    formFields.append(aField)
    aField = {
        "fieldName":"CreatedTime",
        "fieldValueType":"string",
        "fieldConfidence":0.99,
        "fieldValue":f'{createdDateTime}'
    }
    formFields.append(aField)
    # TODO: Implement document confidence computation
    #    "documentConfidence":0.99,
    aDocument = {
        "documentId":0,
        "documentType":f'{modelId}',
        "fields":formFields
    }
    formDocuments.append(aDocument)
    logging.info(f'{fName}formDocuments:{formDocuments}')
    return cuAPIVersion, modelId, isHandwritten, formDocuments
    
def getExtractsFromModel(url, documentCategories, fName):
    fName = f'{fName}f(getExtractsFromModel)->'
    try:
        # If video call Content Understanding
        if containsCategory(documentCategories, "video-", fName):
            return getExtractsFromVideo(url, fName)
        # If it is an image then call the GPT-4o 
        elif containsCategory(documentCategories, "image-", fName):
            return getExtractsFromImage(url, documentCategories, fName)
        else:
            # Check if this class of the document maps to a custom Form Recognizer model or not
            theModelType, extractionModel = getDocumentExtractionModelFromClasses(documentCategories, fName)
    except Exception as e:
        logging.error(f'{fName}Getting right extraction model for the attachment raised exception {e}')
        raise
    # vvvv TODO: Move this logic into separate functions called by Logic App vvvv #
    if theModelType == "unknown-model":
        logging.info(f'{fName}Getting extracts for mode type:{theModelType}')
        return extractResultForUnknownModel(url, fName)
    else:
        return extractResultForCustomModel(extractionModel, url, fName)
    # ^^^^ TODO: Move this logic into separate functions called by Logic App ^^^^ #
    
def extractResultForUnknownModel(ur, fName):
    fName = f'{fName}extractResultForUnknownModel->'
    # frAPIVersion, modelId, isHandwritten, frExtracts
    return "Unknown-v0", "NotYeyImplemented", True, None

def extractResultForCustomModel(extractionModel, url, fName):
    fName = f'{fName}extractResultForCustomModel->'
    logging.info(f'{fName}Getting client to talk to Document Intelligence Service, for extraction model:{extractionModel}')
    formRecognizerApiKey = os.getenv('DOCAI_DOCINTEL_API_KEY')
    formRecognizerCredential = docintel.getDocumentIntelligenceCredential(formRecognizerApiKey)
    formRecognizerClient = docintel.getDocumentIntelligenceClient(
                            endpoint=os.getenv('DOCAI_DOCINTEL_API_ENDPOINT'),
                            credential=formRecognizerCredential
                        )
    logging.info(f'{fName}Calling Document Intelligence Service model:{extractionModel} to extract {url}')
    frAPIVersion, modelId, isHandwritten, result = docintel.extractResultFromOnlineDocument(
                                                        client=formRecognizerClient,
                                                        model=extractionModel,
                                                        url=url
                                                    )
    logging.info(f'{fName}Document Intelligence call returned \
        \nversion:{frAPIVersion}\
        \nmodelId:{modelId}\
        \nisHandwritten:{isHandwritten}')
    logging.debug(f'{fName}result:{result}')
    
    formDocuments = []
    try:
        SYMBOL_OF_TABLE_TYPE = "array"
        SYMBOL_OF_OBJECT_TYPE = "object"
        KEY_OF_VALUE_OBJECT = "valueObject"
        KEY_OF_CELL_CONTENT = "content"     
           
        for idx, aDocument in enumerate(result.documents):
            formFields = []
            for name, field in aDocument.fields.items():
                field_value = field.get("valueString") if field.get("valueString") else field.content
                aField = {
                    "fieldName":f'{name}',
                    "fieldValueType":f'{field.type}',
                    "fieldConfidence": field.confidence,
                    "fieldValue": field_value
                }
                formFields.append(aField)
            # TODO: Implement table reads
            # Call the table extraction function
            #formTables = extractTables(aDocument.tables)
            aDocument = {
                "documentId":idx,
                "documentType": aDocument.doc_type,
                "documentConfidence":aDocument.confidence,
                "fields":formFields
                #"tables":formTables
            }
            formDocuments.append(aDocument)
        logging.info(f'{fName}formDocuments:{formDocuments}')
    except Exception as e:
        logging.warning(f'{fName}Reading form fields raised exception:{e}')
    return frAPIVersion, modelId, isHandwritten, formDocuments
        
def getJsonResponse(doc, fName):
    fName = f'{fName}f(getJsonResponse)->'
    logging.info(f'{fName}Creating response payload json')
    try:
        inJSON = json.dumps(doc)
        logging.info(f'{fName}Document to store in CosmosDB: {inJSON}')
        return inJSON
    except Exception as je:
        errorMessage = f'{fName}Error: json dump raised exception:{je}'
        logging.error(errorMessage)
        raise

def determineAttachmentCategories(reqBody, fName):
    fName = f"{fName}f(determineAttachmentCategories)->"
    categories = [
                {
                    "category": "unknown"
                }
              ]
    try:
        frCategoriesResponseObj = getItemFromRequestBody(reqBody, 'formRecognizerCategories', fName)
        try:
            # Assuming classes was passed as a string
            frCategoriesResponse = ast.literal_eval(frCategoriesResponseObj)
        except:
            # Nope, it was passed as a list
            frCategoriesResponse = frCategoriesResponseObj   

        if frCategoriesResponse:
            frCategories = frCategoriesResponse[0]['category']
            if frCategories != 'unknown':
                logging.info(f'{fName}Found Form Recognizer classified categories:{frCategoriesResponseObj}')
                return frCategoriesResponseObj
    except Exception as e:
        logging.info(f'{fName}Could not find a category from FR. Got exception:{e}')
    try:
        aoaiCategoriesResponseObj = getItemFromRequestBody(reqBody, 'openAICategories', fName)
        try:
            # Assuming classes was passed as a string
            aoaiCategoriesResponse = ast.literal_eval(aoaiCategoriesResponseObj)
        except:
            # Nope, it was passed as a list
            aoaiCategoriesResponse = aoaiCategoriesResponseObj
        if aoaiCategoriesResponse:
            aoaiCategories = aoaiCategoriesResponse[0]['category']
            logging.info(f'{fName}Found GPT4 vision classified categories:{aoaiCategoriesResponseObj}')
            return aoaiCategoriesResponseObj   
    except Exception as e:
        logging.info(f'{fName}Could not find a category from GPT4. Got exception:{e}')
    return f'{categories}'

@app.route(route="getEmailClass", auth_level=func.AuthLevel.ANONYMOUS)
def getEmailClass(req: func.HttpRequest) -> func.HttpResponse:
    fName = f"f(getEmailClass)->"
    try:
        messageId, reqBody = getHttpRequestBody(request = req, functionName=fName)
        logging.info(f'{fName}Received MessageId:{messageId}')
        logging.info(f'{fName}Received request body:{reqBody}')
    except Exception as httpRequestErrorMessage:
        return func.HttpResponse(f'{httpRequestErrorMessage}', status_code=400)
    try:
        messageType = getItemFromRequestBody(reqBody, 'messageType', fName)
        emailPlainBody = getItemFromRequestBody(reqBody, 'body', fName)
        emailSubject = getItemFromRequestBody(reqBody, 'subject', fName)
        if messageType == 'email-body':
            emailCategories = getEmailClassesFromOpenAI(emailSubject, emailPlainBody, fName)
        else:
            errorMessage = f'{fName}ERROR: incorrect messageType {messageType}'
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
    except Exception as httpRequestErrorMessage:
        errorMessage = f'{fName}ERROR: Hit exception trying to read request body items. Exception:{httpRequestErrorMessage}'
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    logging.info(f'{fName}Success: categories: {emailCategories}')
    # Create Json Response or return http 400 if failed
    try:
        responseJson = getJsonResponse(emailCategories, fName)
    except Exception as je:
        return func.HttpResponse(f'{je}', status_code=400)
    
    return func.HttpResponse(responseJson, status_code=200)

@app.route(route="saveEmailProperties", auth_level=func.AuthLevel.ANONYMOUS)
@app.queue_output(arg_name="msg", queue_name="outqueue", connection="AzureWebJobsStorage")
@app.cosmos_db_output(arg_name="outputDocument", database_name="DocAIDatabase", 
    container_name="EmailExtracts", connection="CosmosDbConnectionString")
def saveEmailProperties(req: func.HttpRequest,
                        msg: func.Out[func.QueueMessage], 
                        outputDocument: func.Out[func.Document]) -> func.HttpResponse:
    fName = f"f(saveEmailProperties)->"
    try:
        messageId, reqBody = getHttpRequestBody(request = req, functionName=fName)
        logging.info(f'{fName}Received MessageId:{messageId}')
        logging.info(f'{fName}Received request body:{reqBody}')
    except Exception as httpRequestErrorMessage:
        return func.HttpResponse(f'{httpRequestErrorMessage}', status_code=400)
    try:
        messageType = getItemFromRequestBody(reqBody, 'messageType', fName)        
        if messageType == 'email-body':
            receivedTimeFolder = getItemFromRequestBody(reqBody, 'receivedTimeFolder', fName)
            receivedTime = getItemFromRequestBody(reqBody, 'receivedTime', fName)
            sender = getItemFromRequestBody(reqBody, 'sender', fName)
            emailClasses = getItemFromRequestBody(reqBody, 'categories', fName)
            messageUri = getItemFromRequestBody(reqBody, 'uri', fName)
            url = messageUri + sender + "/" + receivedTimeFolder + "/" + "EmailBody.msg"
            isHTML = getItemFromRequestBody(reqBody, 'isHTML', fName)
            bodyPreview = getItemFromRequestBody(reqBody, 'bodyPreview', fName)
            subject = getItemFromRequestBody(reqBody, 'subject', fName)
            hasAttachment = getItemFromRequestBody(reqBody, 'hasAttachment', fName)
        else:
            errorMessage = f'{fName}ERROR: incorrect messageType {messageType}'
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
    except Exception as httpRequestErrorMessage:
        errorMessage = f'{fName}ERROR: Read request body items raised exception:{httpRequestErrorMessage}'
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    doc = {
        "id":str(uuid.uuid4()),
        "upsertTime":getCurrentUTCTimeString(),
        "messageId":messageId,
        "messageType":messageType,
        "receivedTime":receivedTime,
        "receivedTimeFolder":receivedTimeFolder,
        "sender":sender,
        "categories":emailClasses,
        "url":url,
        "isHTML":isHTML,
        "bodyPreview":bodyPreview,
        "subject":subject,
        "hasAttachment":hasAttachment
    }
    
    # Create Json Response or return http 400 if failed
    try:
        responseJson = getJsonResponse(doc, fName)
    except Exception as je:
        return func.HttpResponse(f'{je}', status_code=400)

    outputDocument.set(func.Document.from_json(responseJson))
    msg.set(messageId)
    responseMessage = f'{fName}messageId {messageId} data stored in Cosmos DB'
    logging.info(responseMessage)
    return func.HttpResponse(responseMessage, status_code=201)

@app.route(route="getAttachmentClassUsingFormRecognizerCustomModel", auth_level=func.AuthLevel.ANONYMOUS)
def getAttachmentClassUsingFormRecognizerCustomModel(req: func.HttpRequest) -> func.HttpResponse:
    fName = f"f(getAttachmentClassUsingFormRecognizerCustomModel)->"
    try:
        messageId, reqBody = getHttpRequestBody(request = req, functionName=fName)
        logging.info(f'{fName}Received MessageId:{messageId}')
        logging.info(f'{fName}Received request body:{reqBody}')
    except Exception as httpRequestErrorMessage:
        return func.HttpResponse(f'{httpRequestErrorMessage}', status_code=400)
    try:
        messageType = getItemFromRequestBody(reqBody, 'messageType', fName)
        receivedTimeFolder = getItemFromRequestBody(reqBody, 'receivedTimeFolder', fName)
        sender = getItemFromRequestBody(reqBody, 'sender', fName)
        messageUri = getItemFromRequestBody(reqBody, 'uri', fName)
        attachmentName = getItemFromRequestBody(reqBody, 'attachmentName', fName)       
        url = messageUri + sender + "/" + receivedTimeFolder + "/attachments/" + attachmentName
        if messageType == 'email-attachment':
            try:
                formRecognizerClassifierModelId = os.getenv('DOCAI_DOCINTEL_CLASSIFIER_ID')
                attachmentClasses = getAttachmentClassesFromFormRecognizer(
                                        url, 
                                        formRecognizerClassifierModelId,
                                        fName
                                    )
            except Exception as e:
                return func.HttpResponse(f'{e}', status_code=400)               
        else:
            errorMessage = f'{fName}ERROR: incorrect messageType {messageType}'
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
    except Exception as httpRequestErrorMessage:
        errorMessage = f'{fName}ERROR: Read request body items raised exception:{httpRequestErrorMessage}'
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    logging.info(f'{fName}Success: attachment {attachmentName} has the classes: {attachmentClasses}') 
    return func.HttpResponse(f'{attachmentClasses}', status_code=200)    

# Use this to classify video files
def getAttachmentClassFromVideo(fName, url):
    fName = f"{fName}f(getAttachmentClassFromVideo)->"
    category = f'video-{url.lower()[-3:]}'
    logging.info(f'{fName}Category of the video file is {category}')  
    return category

# Use this to classify image files
def getAttachmentClassFromImage(fName, url):
    fName = f"{fName}f(getAttachmentClassFromImage)->"
    aoaiMultiModalAPIKey = os.getenv('DOCAI_IMAGE_API_KEY')
    aoaiMultiModalAPIEndpoint = os.getenv('DOCAI_IMAGE_API_ENDPOINT')
    aoaiOmniAPIVersion = os.getenv('DOCAI_IMAGE_API_VERSION')
    aoaiOmniAPIEngine = os.getenv('DOCAI_IMAGE_API_ENGINE')

    try:
        aoai_status, aoai_client = aoai.setupOpenai(
                                            aoaiMultiModalAPIEndpoint,
                                            aoaiMultiModalAPIKey,
                                            aoaiOmniAPIVersion
                                        )
        if aoai_status == True:
            logging.info(f'{fName}OpenAI connection setup successful')
            rawToken = str(os.getenv('DOCAI_BLOB_STORE_SAS_TOKEN'))
            decodedBytes = base64.b64decode(rawToken)
            blobStoreSASToken = decodedBytes.decode("utf-8")[2:-2]
            logging.info(f'{fName}Blob SAS token:{blobStoreSASToken}')
                 
            gotPrompt = composeMultiModalPrompt(f'{url}?{blobStoreSASToken}', fName)
            logging.info(f'{fName}Got Prompt: {gotPrompt}')
            
            tokens_used, finish_reason, completion = aoai.getChatCompletion(
                                                                the_client=aoai_client,
                                                                the_model=aoaiOmniAPIEngine, 
                                                                the_messages=gotPrompt)
            logging.info(f'{fName} GPT4o API Completion succeeded:tokens used:{tokens_used}')
        else:
            errorMessage = f'{fName}ERROR: OpenAI connection setup raised exception:{e}'
            logging.error(errorMessage)
            raise ValueError(errorMessage)
    except Exception as e:
        errorMessage = f'{fName}ERROR: OpenAI raised exception:{e}'
        logging.error(errorMessage)
        raise    
    category = completion.lower()
    if category == 'medical-rx-note' or category == 'medical-lab-report' or category == 'financial-report' or category == 'home' or category == 'automobile' or category == 'other':
        category = f'image-{category}'
    else:
        category = 'image-other'
    logging.info(f'{fName}Category from GPT4o API Completion message:{category}')
    return category

@app.route(route="getAttachmentClassUsingOpenAI", auth_level=func.AuthLevel.ANONYMOUS)
def getAttachmentClassUsingOpenAI(req: func.HttpRequest) -> func.HttpResponse:
    fName = f"f(getAttachmentClassUsingOpenAI)->"
    categories = [
                {
                    "category": "unknown"
                }
              ]
    try:
        messageId, reqBody = getHttpRequestBody(request = req, functionName=fName)
        logging.info(f'{fName}Received MessageId:{messageId}')
        logging.info(f'{fName}Received request body:{reqBody}')
    except Exception as httpRequestErrorMessage:
        return func.HttpResponse(f'{httpRequestErrorMessage}', status_code=400)
    try:
        messageType = getItemFromRequestBody(reqBody, 'messageType', fName)
        receivedTimeFolder = getItemFromRequestBody(reqBody, 'receivedTimeFolder', fName)
        sender = getItemFromRequestBody(reqBody, 'sender', fName)
        messageUri = getItemFromRequestBody(reqBody, 'uri', fName)
        attachmentName = getItemFromRequestBody(reqBody, 'attachmentName', fName)       
        url = messageUri + sender + "/" + receivedTimeFolder + "/attachments/" + attachmentName
            
        # Verify if filename extension is either .png, .jpg, .gif, .webp,
        # .mov or .mp4 (these are supported in GPT4 preview)
        # Also it supports file sizes lower than 20MB
        # TODO: verify using some tool to actually inspect the file content to determine type. 
        #       Check from PIL import Image, import imghdr etc... or other package
        if not url.lower().endswith(('.png', '.jpg', '.jpeg', '.webp', '.gif', '.mov', '.mp4')):
            logging.info(f'{fName}Unsupported image file. Only .png, .jpg, .jpeg, .webp, .mp4, .mov are only supported by GPT4 Turbo with Vision model at this time')
            return func.HttpResponse(f'{categories}', status_code=200)    
        if messageType == 'email-attachment':
            try:
                if url.lower().endswith(('.mov', '.mp4')):
                    #classifiedCategory
                    # Process video files. For video we can't do realtime Content Understanding
                    # calls to get more data about the video. So, just adding the extension 
                    # as part of category
                    # Else process image files
                    category = getAttachmentClassFromVideo(fName, url)
                else:
                    # classify images
                    category = getAttachmentClassFromImage(fName, url)
                categories.clear()
                aClassInfo = {"category":f"{category}"}
                categories.append(aClassInfo)
            except Exception as e:
                return func.HttpResponse(f'{e}', status_code=400)               
        else:
            errorMessage = f'{fName}ERROR: incorrect messageType {messageType}'
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
    except Exception as httpRequestErrorMessage:
        errorMessage = f'{fName}ERROR: Read request body items raised exception:{httpRequestErrorMessage}'
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)

    logging.info(f'{fName}Success: attachment {attachmentName} categories: {categories}') 
    return func.HttpResponse(f'{categories}', status_code=200)    

@app.route(route="getAttachmentClass", auth_level=func.AuthLevel.ANONYMOUS)
def getAttachmentClass(req: func.HttpRequest) -> func.HttpResponse:
    fName = f"f(getAttachmentClass)->"
    categories = [
                {
                    "category": "unknown"
                }
              ]   
    logging.info(f'{fName}HttpRequest:{func.HttpRequest}')
    try:
        messageId, reqBody = getHttpRequestBody(request = req, functionName=fName)
        logging.info(f'{fName}Received MessageId:{messageId}')
        logging.info(f'{fName}Received request body:{reqBody}')
    except Exception as httpRequestErrorMessage:
        return func.HttpResponse(f'{httpRequestErrorMessage}', status_code=400)
    try:
        # Read categories returned by FR and AOAI and finalize the 
        # true categories for the attachment
        messageType = getItemFromRequestBody(reqBody, 'messageType', fName)
        if messageType == 'email-attachment':
            try:
                gotCategories = determineAttachmentCategories(reqBody, fName)      
                if gotCategories:
                    logging.info(f'Determined attachment categories : {gotCategories}')
                    return func.HttpResponse(gotCategories, status_code=200)
                else:
                    logging.info(f'{fName}Could not determine attachment category, returning unknown')
                    return func.HttpResponse(categories, status_code=200)
            except Exception as e:
                    logging.info(f'{fName}Determining attachment category raised exception:{e}')
                    return func.HttpResponse(categories, status_code=200)
        else:
            errorMessage = f'{fName}ERROR: incorrect messageType {messageType}'
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
    except Exception as httpRequestErrorMessage:
        errorMessage = f'{fName}ERROR: Read request body items raised exception:{httpRequestErrorMessage}'
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)
    
@app.route(route="saveAttachmentProperties", auth_level=func.AuthLevel.ANONYMOUS)
@app.queue_output(arg_name="msg", queue_name="outqueue", connection="AzureWebJobsStorage")
@app.cosmos_db_output(arg_name="outputDocument", database_name="DocAIDatabase", 
    container_name="EmailExtracts", connection="CosmosDbConnectionString")
def saveAttachmentProperties(req: func.HttpRequest,
                        msg: func.Out[func.QueueMessage], 
                        outputDocument: func.Out[func.Document]) -> func.HttpResponse:
    fName = f"f(saveAttachmentProperties)->"
    try:
        messageId, reqBody = getHttpRequestBody(request = req, functionName=fName)
        logging.info(f'{fName}Received MessageId:{messageId}')
        logging.info(f'{fName}Received request body:{reqBody}')
    except Exception as httpRequestErrorMessage:
        return func.HttpResponse(f'{httpRequestErrorMessage}', status_code=400)
    try:
        messageType = getItemFromRequestBody(reqBody, 'messageType', fName)
        if messageType == 'email-attachment':
                receivedTimeFolder = getItemFromRequestBody(reqBody, 'receivedTimeFolder', fName)
                receivedTime = getItemFromRequestBody(reqBody, 'receivedTime', fName)
                sender = getItemFromRequestBody(reqBody, 'sender', fName)
                attachmentClasses = getItemFromRequestBody(reqBody, 'categories', fName)
                attachmentName = getItemFromRequestBody(reqBody, 'attachmentName', fName)               
                messageUri = getItemFromRequestBody(reqBody, 'uri', fName)
                url = messageUri + sender + "/" + receivedTimeFolder + "/attachments/" + attachmentName
        else:
            errorMessage = f'{fName}ERROR: incorrect messageType {messageType}'
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
    except Exception as httpRequestErrorMessage:
        errorMessage = f'{fName}ERROR: Read request body items raised exception:{httpRequestErrorMessage}'
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)
    # If this is a GPT4 detected category
    # call GPT-4o to get description from image
    if containsCategory(attachmentClasses, "image-", fName):
        theModelType = "gpt4-o"
    elif containsCategory(attachmentClasses, "video-", fName):
        theModelType = "content-understanding"
    else:
        try:
            theModelType = getDocumentExtractionModelFromClasses(attachmentClasses, fName)
        except:
            theModelType = "unknown"
        if theModelType != "unknown":
            theModelType = "custom-model"
        else:
            theModelType = "unknown-model"
    doc = {
        "id":str(uuid.uuid4()),
        "upsertTime":getCurrentUTCTimeString(),
        "messageId":messageId,
        "messageType":messageType,
        "receivedTime":receivedTime,
        "receivedTimeFolder":receivedTimeFolder,
        "sender":sender,
        "categories":attachmentClasses,
        "modelType":theModelType,
        "attachmentName":attachmentName,
        "url":url
    }
    
    # Create Json Response or return http 400 if failed
    try:
        responseJson = getJsonResponse(doc, fName)
    except Exception as je:
        return func.HttpResponse(f'{je}', status_code=400)

    outputDocument.set(func.Document.from_json(responseJson))
    msg.set(messageId)
    responseMessage = f'{fName}messageId  {messageId} data stored in Cosmos DB'
    logging.info(responseMessage)
    return func.HttpResponse(responseMessage, status_code=201)

@app.route(route="extractAttachmentData", auth_level=func.AuthLevel.ANONYMOUS)
@app.queue_output(arg_name="msg", queue_name="outqueue", connection="AzureWebJobsStorage")
@app.cosmos_db_output(arg_name="outputDocument", database_name="DocAIDatabase", 
    container_name="EmailExtracts", connection="CosmosDbConnectionString")
def extractAttachmentData(req: func.HttpRequest,
                        msg: func.Out[func.QueueMessage], 
                        outputDocument: func.Out[func.Document]) -> func.HttpResponse:
    fName = f"f(extractAttachmentData)->"
    try:
        messageId, reqBody = getHttpRequestBody(request = req, functionName=fName)
        logging.info(f'{fName}Received MessageId:{messageId}')
        logging.info(f'{fName}Received request body:{reqBody}')
    except Exception as httpRequestErrorMessage:
        return func.HttpResponse(f'{httpRequestErrorMessage}', status_code=400)
    try:
        messageType = getItemFromRequestBody(reqBody, 'messageType', fName)
        if messageType == 'email-attachment-extracts':
                receivedTimeFolder = getItemFromRequestBody(reqBody, 'receivedTimeFolder', fName)
                sender = getItemFromRequestBody(reqBody, 'sender', fName)
                attachmentName = getItemFromRequestBody(reqBody, 'attachmentName', fName)               
                attachmentClasses = getItemFromRequestBody(reqBody, 'categories', fName)
                messageUri = getItemFromRequestBody(reqBody, 'uri', fName)
                url = messageUri + sender + "/" + receivedTimeFolder + "/attachments/" + attachmentName
                frAPIVersion, modelId, isHandwritten, frExtracts = getExtractsFromModel(url, attachmentClasses, fName)
        else:
            errorMessage = f'{fName}ERROR: incorrect messageType {messageType}'
            logging.error(errorMessage)
            return func.HttpResponse(errorMessage, status_code=400)
    except Exception as httpRequestErrorMessage:
        errorMessage = f'{fName}ERROR: Form extraction raised exception:{httpRequestErrorMessage}'
        logging.error(errorMessage)
        return func.HttpResponse(errorMessage, status_code=400)
    logging.info(f'{fName}From Document Intelligence created filtered extracts:{frExtracts}')
    doc = {
        "id":str(uuid.uuid4()),
        "upsertTime":getCurrentUTCTimeString(),
        "messageId":messageId,
        "messageType":messageType,
        "attachmentName":attachmentName,
        "url":url,
        "frAPIVersion": frAPIVersion,
        "modelId": modelId,
        "isHandwritten": isHandwritten,
        "extracts": frExtracts
    }
    # Create Json Response or return http 400 if failed
    try:
        responseJson = getJsonResponse(doc, fName)
    except Exception as je:
        return func.HttpResponse(f'{je}', status_code=400)

    outputDocument.set(func.Document.from_json(responseJson))
    msg.set(messageId)
    responseMessage = f'{fName}messageId  {messageId} data stored in Cosmos DB'
    logging.info(responseMessage)
    return func.HttpResponse(responseMessage, status_code=201)

