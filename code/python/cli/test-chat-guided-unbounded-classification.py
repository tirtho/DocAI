import os, sys
import aoai
from utils import *

# TODO: check why outlook python package is not able to open .msg file

COMMANDLINE_SHORT_OPTIONS = 'he:m:v:s:' 
COMMANDLINE_LONG_OPTIONS = ['help', 'endpoint=', 'model=', 'version=', 'message=']
HELP_TEXT = "  -h/--help for help\n\
  -e/--endpoint <OpenAI endpoint>\n\
  -m/--model <deployed model/engine>\n\
  -v/--version <OpenAI API version>\n\
  -s/--message <the message to classify>\n"

#cli_endpoint = ''
#cli_engine = ''
#cli_version = ''
#cli_message = ''

def main():

  get_cli_args()
  status, aoai_client = aoai.setupOpenai(cli_endpoint, cli_version)
  messages = [{"role":"system","content":"You are an AI assistant that helps people find information."},
              {"role":"user","content":"Headline: Major Retailer Announces Plans to Close Over 100 Stores\nCategory:"},
              {"role":"assistant","content":"Business & Finance"},
              {"role":"user","content":"Headline: The Republican and Democratic parties are getting ready for the 2024 Presidential Election.\nCategory:"},
              {"role":"assistant","content":"Politics"},
              {"role":"user","content":"Headline: Argentina won the World Cup Soccer match in 2022\nCategory:"},
              {"role":"assistant","content":"Sports"},
              {"role":"user","content":f"Headline: {cli_message}\nCategory:"},
              {"role":"assistant","content":""}
            ]
  tokens_used, finish_reason, classified_category = aoai.getChatCompletion(
                                                            the_client = aoai_client, 
                                                            the_model = cli_engine, 
                                                            the_messages = messages)
  print(f"Tokens: {tokens_used}")
  print(f"Finish Reason: {finish_reason}")
  print(f"Category: {classified_category}")

def get_cli_args():
  # If asked for help
  if needHelp(COMMANDLINE_SHORT_OPTIONS, COMMANDLINE_LONG_OPTIONS, HELP_TEXT) == True:
     print("{} - Help information above".format(getThisRunningFileName()))
     exit()
  # Now get the value for a given argument
  # Pass either or both short and long name of argument
  # If you do not pass short name, and user passes short name
  # you will not get the value. Same with long name.
  # So it is better to pass both short and long names
  # to catch the value for the argument passed in short or long form by user
  global cli_endpoint
  status, cli_endpoint = getArgumentValue(
                            '-e', '--endpoint', 
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS,
                            f"{os.environ['OPENAI_API_ENDPOINT']}"
                            )
  if cli_endpoint: print(f'OpenAI Endpoint: {cli_endpoint}')
  global cli_engine    
  status, cli_engine = getArgumentValue(
                            '-m', '--model', 
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS,
                            f"{os.environ['OPENAI_API_ENGINE']}"
                            )
  if cli_engine: print(f'OpenAI Deployed model: {cli_engine}')
  global cli_version
  status, cli_version = getArgumentValue(
                            '-v', '--version', 
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS,
                            f"{os.environ['OPENAI_API_VERSION']}"
                            )
  if cli_version: print(f'OpenAI API version: {cli_version}')
  global cli_message
  status, cli_message = getArgumentValue(
                            '-s', '--message', 
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS,
                            'Pele is the greatest soccer player of all times!'
                            )
  if cli_message: print(f'Client message: {cli_message}')

main()