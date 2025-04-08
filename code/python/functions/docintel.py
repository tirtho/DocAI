import logging
from azure.identity import DefaultAzureCredential
from azure.core.credentials import AzureKeyCredential
from azure.ai.documentintelligence import DocumentIntelligenceClient
from azure.ai.documentintelligence import *
from azure.ai.documentintelligence.models import DocumentAnalysisFeature, AnalyzeResult
from azure.ai.documentintelligence.models import AnalyzeDocumentRequest, ClassifyDocumentRequest

def getDocumentIntelligenceCredential(api_key):
  try:
    if api_key and not api_key.isspace():
      # the string is non-empty      
      logging.info("\nGot Azure Form Recognizer API Key from environment variable")
      # Return credential
      return AzureKeyCredential(api_key)
    else:
      # the string is empty
      try:
        logging.info(f"\nCould not get API key from environment variable. Trying Managed ID")
        default_credential = DefaultAzureCredential()
        logging.info("\nAuthenticated successfully with AAD token")
        return default_credential
      except:
        logging.error("\nSomething went wrong getting token with Managed Identity")
        return
  except:
    logging.error("\nSomething went wrong getting access key from environment variable")
    return

def getDocumentIntelligenceClient(endpoint, credential):
  return DocumentIntelligenceClient(endpoint, credential)

# Extract from local file
def extractResultFromLocalDocument(client, model, filepath):
    with open(filepath, "rb") as f:
        poller = client.begin_analyze_document(
                            model_id=model, 
                            body=f,
                            features=[DocumentAnalysisFeature.STYLE_FONT, 
                                      DocumentAnalysisFeature.OCR_HIGH_RESOLUTION]
                 )
    result: AnalyzeResult = poller.result()
    return getExtract(result)

# Extract from Online File (e.g. from Blob Store)
def extractResultFromOnlineDocument(client, model, url):
  poller = client.begin_analyze_document(
                      model_id=model, 
                      body=AnalyzeDocumentRequest(url_source=url),
                      features=[DocumentAnalysisFeature.STYLE_FONT, 
                                DocumentAnalysisFeature.OCR_HIGH_RESOLUTION]
              )
  result = poller.result()
  return getExtract(result)

# Get metadata and results from extracted document
# Returns -
# Azure Document Intelligence API version
# Model Id
# hand_wrtten = True, If there is any hand written text found in document with confidence score of > 0.5
# result
def getExtract(result):
    if result.styles and any([style.is_handwritten for style in result.styles]):
        hand_written = True
    else:
        hand_written = False
    return result.api_version, result.model_id, hand_written, result

def classifyLocalDocument(client, model, file_path):
  import os
  with open(file_path, "rb") as f:
    poller = client.begin_classify_document(
                      classifier_id=model,
                      split="perPage", # could be "auto", "none" and "perPage". Default is None
                      body=f
              )
  return poller.result()

def classifyOnlineDocument(client, model, file_url):
  poller = client.begin_classify_document(
                  classifier_id=model,
                  split="perPage", # could be "auto", "none" and "perPage". Default is None
                  body=ClassifyDocumentRequest(url_source=file_url)
              )
  return poller.result()

def getCategories(result, confidence_threshold):
    import json
    categories = [
        {
            "category": "unknown"
        }
    ]
    if result and result.documents:
        categories.clear()
        for doc in result.documents:
            pagesClassifiedArray = [region.page_number for region in doc.bounding_regions]
            pagesClassifiedJson = json.dumps(pagesClassifiedArray)
            theCategory = doc.doc_type
            if doc.confidence < confidence_threshold:
                # The doc class is unknown to Form Recognizer
                # Need to try AOAI and other ways to determine the class
                logging.info(f'Confidence of {doc.confidence} is low in determining category of document.')
                theCategory = "unknown"
            aClassInfo = {
                    "category":theCategory,
                    "confidence":doc.confidence,
                    "pages":pagesClassifiedJson
            }
            categories.append(aClassInfo)
    return categories

def getLabeledDataFromExtractedDocument(document):
  return document.doc_type, document.confidence, document.fields.items