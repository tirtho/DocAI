import os
from azure.identity import DefaultAzureCredential
from azure.core.credentials import AzureKeyCredential

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
        #for access to Key Vault get_token(https://vault.azure.net)
        #token = default_credential.get_token("https://cognitiveservices.azure.com/.default")
        print("\nAuthenticated successfully with AAD token")
        return default_credential
        #return AzureKeyCredential(token.token)
      except:
        print("\nSomething went wrong getting token with Managed Identity")
        return
  except:
    print("\nSomething went wrong getting access key from environment variable")
    return
