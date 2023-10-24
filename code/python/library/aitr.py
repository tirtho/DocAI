import os
from azure.identity import DefaultAzureCredential
from azure.ai.translation.text import TextTranslationClient, TranslatorCredential
from azure.ai.translation.text.models import InputTextItem

def getAITranslatorCredential(deployed_region):
  try:
    api_key = os.getenv("AI_TRANSLATOR_API_KEY")
    if api_key and not api_key.isspace():
      # the string is non-empty      
      print("\nGot Azure AI Translator API Key from environment variable")
      # Return credential
      return TranslatorCredential(api_key, deployed_region)
    else:
      # the string is empty
      try:
        print(f"\nCould not get API key from environment variable AI_TRANSLATOR_API_KEY. Trying Managed ID")
        default_credential = DefaultAzureCredential()
        print("\nAuthenticated successfully with AAD token")
        token = default_credential.get_token("https://cognitiveservices.azure.com/.default")
        return TranslatorCredential(token.token, deployed_region)
      except:
        print("\nSomething went wrong getting token with Managed Identity")
        return
  except:
    print("\nSomething went wrong getting access key from environment variable")
    return

def getAITranslatorClient(endpoint, credential):
  return TextTranslationClient(endpoint=endpoint, credential=credential)

def translate(translator, content, to_lang):
  contents = [ InputTextItem(text = content) ]
  to_languages = [to_lang]
  response = translator.translate(
                            content=contents, 
                            to=to_languages)
  translation_response = response[0] if response else None
  translated_text = ''
  if translation_response:
    for translation in translation_response.translations:
      translated_text = translated_text + translation.text
  return translated_text