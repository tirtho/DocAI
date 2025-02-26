from azure.identity import DefaultAzureCredential, get_bearer_token_provider
from openai import AzureOpenAI
import os
import requests

# Returns a +ve number if successful
# It sets openai with the endpoint, api key etc.
# TODO: stop dumping exception into stdout
def setupOpenai(aoai_endpoint, aoai_api_key, aoai_version):
  try:
    #--- api_key = os.getenv("OPENAI_API_KEY")
    if aoai_api_key and not aoai_api_key.isspace():
      # the string is non-empty
      client = AzureOpenAI(
                api_key=aoai_api_key,
                azure_endpoint=aoai_endpoint,
                api_version=aoai_version
              )
      print("\nGot OPENAI API Key from environment variable")
      return True, client
    else:
      # the string is empty
      try:
        print(f"\nCould not get API key from environment variable OPENAI_API_KEY. Trying Managed ID")
        token_provider = get_bearer_token_provider (
          DefaultAzureCredential(),
          "https://cognitiveservices.azure.com/.default"          
        )
        client = AzureOpenAI(
          azure_ad_token_provider=token_provider,
          azure_endpoint=aoai_endpoint,
          api_version=aoai_version
        )
        print("\nAuthenticated successfully with AAD token")
        return True, client
      except:
        print("\nSomething went wrong getting token with Managed Identity")
        return False, None
  except:
    print("\nSomething went wrong getting access key from environment variable")
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

# Find cosine similarty between the vectors
import numpy as np
def cosine_similarity(a, b):
    return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))

# Return vectors for a given text using the ADA model
def generate_embedding(the_client, the_model, the_text):
    response = the_client.embeddings.create(
                  input=the_text, 
                  model=the_model
                )
    return response.data[0].embedding