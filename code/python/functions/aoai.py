from azure.identity import DefaultAzureCredential
import openai
import os
import requests

# Returns a +ve number if successful
# It sets openai with the endpoint, api key etc.
# TODO: stop dumping exception into stdout
def setupOpenai(aoai_endpoint, aoai_version):
  openai.api_base = aoai_endpoint
  openai.api_version = aoai_version
  try:
    api_key = os.getenv("OPENAI_API_KEY")
    if api_key and not api_key.isspace():
      # the string is non-empty
      openai.api_type = "azure"
      openai.api_key = api_key
      print("\nGot OPENAI API Key from environment variable")
      return 1
    else:
      # the string is empty
      try:
        print(f"\nCould not get API key from environment variable OPENAI_API_KEY. Trying Managed ID")
        default_credential = DefaultAzureCredential()
        token = default_credential.get_token("https://cognitiveservices.azure.com/.default")
        openai.api_type = "azure_ad"
        openai.api_key = token.token
        print("\nAuthenticated successfully with AAD token")
        return 2
      except:
        print("\nSomething went wrong getting token with Managed Identity")
        return -2
  except:
    print("\nSomething went wrong getting access key from environment variable")
    return -1

# Returns total tokens used and the chat completion
def getChatCompletion(the_engine, the_messages):
    response = openai.ChatCompletion.create(
        engine=the_engine,
        messages=the_messages,
        temperature=0,
        max_tokens=1000,
        #top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        stop=None
    )
    return response.usage.total_tokens, response.choices[0].finish_reason, response.choices[0].message["content"]

# For models called using Http requests
def getHttpChatCompletion(
      fName, 
      aoaiEndpointEnv, 
      aoaiKeyEnv, 
      aoaiAPIVersionEnv, 
      aoaiAPIEngineEnv, 
      prompt
    ):
    fName = f"{fName}f(getHttpChatCompletion)->"
    try:
      if aoaiKeyEnv:
        aoaiAPIKey = os.getenv(aoaiKeyEnv)
      if aoaiAPIKey and not aoaiAPIKey.isspace():
        # the string is non-empty
        print(f'\n{fName}Got API Key from environment variable {aoaiKeyEnv}')
      else:
        # Get key using Managed Identity
        print(f'\n{fName}Could not get API key from environment variable {aoaiKeyEnv}. Trying Managed ID')
        default_credential = DefaultAzureCredential()
        token = default_credential.get_token("https://cognitiveservices.azure.com/.default")
        aoaiAPIKey = token.token
        print(f"\n{fName}Authenticated successfully with AAD token")        
    except:
        print(f'\n{fName}Failed to get AOAI API key')
        return -1

    aoaiAPIEndpoint = os.getenv(aoaiEndpointEnv)
    aoaiAPIVersion = os.getenv(aoaiAPIVersionEnv)
    aoaiAPIEngine = os.getenv(aoaiAPIEngineEnv)
    rawToken = str(os.getenv('BLOB_STORE_SAS_TOKEN'))
    decodedBytes = base64.b64decode(rawToken)
    blobStoreSASToken = decodedBytes.decode("utf-8")[2:-2]
    logging.info(f'{fName}Blob SAS token:{blobStoreSASToken}')        
    endpoint = f'{aoaiMultiModalAPIEndpoint}openai/deployments/{aoaiOmniAPIEngine}/chat/completions?api-version={aoaiOmniAPIVersion}'
    gotPrompt = composeMultiModalPrompt(f'{url}?{blobStoreSASToken}', fName)
    logging.info(f'{fName}Got Prompt: {gotPrompt}')
    headers = {
                "Content-Type": "application/json",   
                "api-key": aoaiMultiModalAPIKey 
            }
    response = runHttpRequest(endpoint=endpoint,
                   headers=headers,
                   requestType="POST",
                   jsonRequestBody=gotPrompt,
                   fName=fName
                )
    #response = requests.post(endpoint, headers=headers, data=json.dumps(gotPrompt))
    jsonResponse = json.loads(response.content.decode('utf-8'))
    logging.info(f'{fName} GPT4o API response:{jsonResponse}')
  

# Return vectors for a given text using the ADA model
def generate_embedding(the_engine, the_text):
    response = openai.Embedding.create(
                  input=the_text, 
                  engine=the_engine)
    embedding = response['data'][0]['embedding']
    return embedding
