from azure.identity import DefaultAzureCredential, get_bearer_token_provider
from openai import AzureOpenAI
import requests
import logging

# Returns a +ve number if successful
# It sets openai with the endpoint, api key etc.
# TODO: stop dumping exception into stdout
def setupOpenai(aoai_endpoint, aoai_api_key, aoai_version):
  try:
    if aoai_api_key and not aoai_api_key.isspace():
      # the string is non-empty
      client = AzureOpenAI(
                api_key=aoai_api_key,
                azure_endpoint=aoai_endpoint,
                api_version=aoai_version
              )
      logging.info(f'Got OPENAI API Key from environment variable')
      return True, client
    else:
      # the string is empty
      try:
        logging.info(f'Environment variable OPENAI_API_KEY is blank. Trying Managed ID')
        token_provider = get_bearer_token_provider (
          DefaultAzureCredential(),
          "https://cognitiveservices.azure.com/.default"          
        )
        client = AzureOpenAI(
          azure_ad_token_provider=token_provider,
          azure_endpoint=aoai_endpoint,
          api_version=aoai_version
        )
        logging.info(f'Authenticated successfully with AAD token')
        return True, client
      except Exception as e:
        logging.info(f'Exception raised when trying to get token with Managed Identity: {e}')
        return False, None
  except Exception as e:
    logging.info(f'Exception raised when trying to get token with API Key: {e}')
    return False, None

# Returns total tokens used and the chat completion
def getChatCompletion(the_client, the_model, the_messages):
    completion = the_client.chat.completions.create(
        model=the_model,
        messages=the_messages,
        temperature=0,
        max_tokens=1000,
        #top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        stop=None
    )
    return completion.usage.total_tokens, completion.choices[0].finish_reason, completion.choices[0].message.content  

# Return vectors for a given text using the ADA model
def generate_embedding(the_client, the_model, the_text):
    response = the_client.embeddings.create(
                  input=the_text, 
                  model=the_model
                )
    return response.data[0].embedding