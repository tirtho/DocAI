from azure.identity import DefaultAzureCredential
import openai

def setup_openai(aoai_endpoint, aoai_version):

    # Request credential
    default_credential = DefaultAzureCredential()
    token = default_credential.get_token("https://cognitiveservices.azure.com/.default")
    # Setup parameters
    openai.api_type = "azure_ad"
    openai.api_key = token.token
    openai.api_base = aoai_endpoint
    openai.api_version = aoai_version
    return

def get_completion(the_user_prompt, the_engine):
    the_messages = [{"role":"system","content":"You are an AI assistant that helps people classify information."},
                    {"role":"user","content":"Headline: I want to submit an auto incident claim. Where can I find the how-to information? My account ID is TRI 12345\nCategory:"},
                    {"role":"assistant","content":"claims-new"},
                    {"role":"user","content":"Headline: I have filed a claim a few days back. I need to change the mobile phone number in that claim. How do I do it?\nCategory:"},
                    {"role":"assistant","content":"claims-update"},
                    {"role":"user","content":"Headline: I have filed a claim a few days back. I found that this is a duplicate of a previously filed claim. I need to cancel the latest claim filed.\nCategory:"},
                    {"role":"assistant","content":"claims-cancel"},
                    {"role":"user","content":"Headline: I live in the suburbs of Boston. The entire New England region is a hot tourist spot during Fall.\nCategory:"},
                    {"role":"assistant","content":"unknown"},
                    {"role":"user","content":f"Headline: {the_user_prompt}\nCategory:"},
                    {"role":"assistant","content":""}                    
                    ]
    response = openai.ChatCompletion.create(
        engine=the_engine,
        messages=the_messages,
        temperature=0,
        max_tokens=1000,
        top_p=1,
        frequency_penalty=0,
        presence_penalty=0,
        stop=None
    )
    return response.choices[0].message["content"]

def classify_zero_shot(the_user_prompt, the_user_endpoint, the_user_version, the_user_engine):
    print(f'Endpoint={the_user_endpoint}, Version={the_user_version}, Engine={the_user_engine}\n')
    setup_openai(aoai_endpoint=the_user_endpoint, aoai_version=the_user_version)
    return get_completion(the_user_prompt=the_user_prompt, the_engine=the_user_engine)

import sys

cli_endpoint = sys.argv[1]
cli_engine = sys.argv[2]
cli_version = sys.argv[3]
cli_message = sys.argv[4]

classified_type = classify_zero_shot(the_user_prompt=cli_message, the_user_endpoint=cli_endpoint, the_user_version=cli_version, the_user_engine=cli_engine)
print(f'Message: {cli_message}\nCategory: {classified_type}')