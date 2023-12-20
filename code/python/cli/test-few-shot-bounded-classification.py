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
  
def main():

  get_cli_args()
  aoai.setupOpenai(cli_endpoint, cli_version)
  my_prompt = [
              {
                "role": "user", 
                "content": f"Classify the following news headline into 1 of the following categories: \
                            Business, Tech, Politics, Sport, Entertainment, Other\
                            \n\nHeadline 1: Donna Steffensen Is Cooking Up a New Kind of Perfection. The Internet's most beloved cooking guru has a \
                            buzzy new book and a fresh new perspective\
                            \nCategory: Entertainment\
                            \n\nHeadline 2: Major Retailer Announces Plans to Close Over 100 Stores\
                            \nCategory: Business\
                            \n\nHeadline 3: {cli_message}\
                            \nCategory:"
                }
              ]      
  tokens_used, finish_reason, classified_category = aoai.getChatCompletion(the_engine=cli_engine, the_messages=my_prompt)
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
                            'tr-gpt4'
                            )
  if cli_engine: print(f'OpenAI Deployed model: {cli_engine}')
  global cli_version
  status, cli_version = getArgumentValue(
                            '-v', '--version', 
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS,
                            '2023-07-01-preview'
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