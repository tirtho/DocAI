import os
from azure.identity import DefaultAzureCredential
from azure.core.credentials import AzureKeyCredential
from azure.ai.formrecognizer import DocumentAnalysisClient

def getFormRecognizerCredential():
  try:
    api_key = os.getenv("FORM_RECOGNIZER_API_KEY")
    if api_key and not api_key.isspace():
      # the string is non-empty      
      print("\nGot Azure Form Recognizer API Key from environment variable")
      # Return credential
      return AzureKeyCredential(api_key)
    else:
      # the string is empty
      try:
        print(f"\nCould not get API key from environment variable FORM_RECOGNIZER_API_KEY. Trying Managed ID")
        default_credential = DefaultAzureCredential()
        print("\nAuthenticated successfully with AAD token")
        return default_credential
      except:
        print("\nSomething went wrong getting token with Managed Identity")
        return
  except:
    print("\nSomething went wrong getting access key from environment variable")
    return

def getDocumentAnalysisClient(endpoint, credential):
  return DocumentAnalysisClient(endpoint, credential)

# Extract from local file
def extractResultFromLocalDocument(client, model, filepath):
    with open(filepath, "rb") as f:
        poller = client.begin_analyze_document(model_id=model, document=f)
    result = poller.result()
    return getExtract(result)

# Extract from Online File (e.g. from Blob Store)
def extractResultFromOnlineDocument(client, model, url):
  poller = client.begin_analyze_document_from_url(model, url)
  result = poller.result()
  return getExtract(result)

# Get metadata and results from extracted document
# Returns -
# Azure Document Intelligence API version
# Model Id
# hand_wrtten = True, If there is any hand written text found in document with confidence score of > 0.5
# result
def getExtract(result):
  hand_written = False
  for idx, style in enumerate(result.styles):
    if style.confidence > 0.5:
      hand_written = True
      break
  return result.api_version, result.model_id, hand_written, result

def getLabeledDataFromExtractedDocument(document):
  return document.doc_type, document.confidence, document.fields.items