#Note: The openai-python library support for Azure OpenAI is in preview.
import os, sys
import openai
from azure.identity import DefaultAzureCredential


default_credential = DefaultAzureCredential()
token = default_credential.get_token("https://cognitiveservices.azure.com/.default")
openai.api_type = "azure_ad"
openai.api_base = "https://tr-non-prod-gpt4.openai.azure.com/"
openai.api_version = "2023-07-01-preview"
openai.api_key = token.token

response = openai.ChatCompletion.create(
  engine="tr-gpt4-32k",
  messages = [{"role":"system","content":"You are an AI assistant that helps people find information."},
              {"role":"user","content":"Headline: Major Retailer Announces Plans to Close Over 100 Stores\nCategory:"},
              {"role":"assistant","content":"Business & Finance"},
              {"role":"user","content":"Headline: The Republican and Democratic parties are getting ready for the 2024 Presidential Election.\nCategory:"},
              {"role":"assistant","content":"Politics"},
              {"role":"user","content":"Headline: Argentina won the World Cup Soccer match in 2022\nCategory:"},
              {"role":"assistant","content":"Sports"},
              {"role":"user","content":f"Headline: {sys.argv[1]}\nCategory:"},
              {"role":"assistant","content":""}
             ],
  temperature=0.7,
  max_tokens=800,
  top_p=0.95,
  frequency_penalty=0,
  presence_penalty=0,
  stop=None)

reply = response.choices[0].message["content"]

print(f"Message: {sys.argv[1]}\nCategory: {reply}")