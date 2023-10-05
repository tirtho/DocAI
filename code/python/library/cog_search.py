import os
from azure.identity import DefaultAzureCredential
from azure.core.credentials import AzureKeyCredential  
from azure.search.documents.indexes import SearchIndexClient  


def getCogSearchCredential():
  try:
    api_key = os.getenv("COG_SEARCH_ADMIN_KEY")
    if api_key and not api_key.isspace():
      # the string is non-empty      
      print("\nGot Azure Cognitive Search ADMIN API Key from environment variable")
      # Return credential
      return AzureKeyCredential(api_key)
    else:
      # the string is empty
      try:
        print(f"\nCould not get API key from environment variable COG_SEARCH_ADMIN_KEY. Trying Managed ID")
        default_credential = DefaultAzureCredential()
        #for access to Key Vault get_token(https://vault.azure.net)
        token = default_credential.get_token("https://cognitivesearch.azure.com/.default")
        print("\nAuthenticated successfully with AAD token")
        return AzureKeyCredential(token.token)
      except:
        print("\nSomething went wrong getting token with Managed Identity")
        return
  except:
    print("\nSomething went wrong getting access key from environment variable")
    return
