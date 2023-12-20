import os, sys
import aoai
import outlook
from utils import *

# TODO: check why outlook python package is not able to open .msg file

COMMANDLINE_SHORT_OPTIONS = 'he:m:v:s:i:f:' 
COMMANDLINE_LONG_OPTIONS = ['help', 'endpoint=', 'model=', 'version=', 'summary-word-count=', 'intent-word-count=', 'email-filepath=']
HELP_TEXT = "  -h/--help for help\n\
  -e/--endpoint <OpenAI endpoint>\n\
  -m/--model <deployed model/engine>\n\
  -v/--version <OpenAI API version>\n\
  -s/--summary-word-count <max words in summary>\n\
  -i/--intent-word-count <max words in intent>\n\
  -f/--email-filepath <filepath to email data>\n"

#cli_endpoint = ''
#cli_engine = ''
#cli_version = ''
#cli_email_summary_word_count = ''
#cli_email_intent_word_count = ''
#cli_email_filepath = ''

def main():

  get_cli_args()
  
  email_subject, email_body, attachment_list = outlook.getEmailSubjectBodyAttachmentList(
                                                          cli_email_filepath
                                                      )  
  #print(f"----------Body: [{email_body}]------------")
  aoai.setupOpenai(cli_endpoint, cli_version)

  #  Provide Summary of Email Body
  print(f'---Email Subject: {email_subject}')
  my_prompt = [
              {
                "role": "user", 
                "content": f"Find the summary of the following email in {cli_email_summary_word_count} words: \
                            \nEmail: {email_body}\
                            \nSummary:"
                }
              ]      
  tokens_used, finish_reason, email_summary = aoai.getChatCompletion(the_engine=cli_engine, 
                                                                     the_messages=my_prompt)
  print(f"\tTokens: {tokens_used}")
  print(f"\tFinish Reason: {finish_reason}")
  print(f"\tSummary: {email_summary}")

  #  Get intent without grounding with an option list
  print(f"---Find Intent in this email strictly in {cli_intent_word_count} words")
  my_prompt = [
              {
                "role": "user", 
                "content": f"Find Insurance Type and Intended Action in the following email in {cli_intent_word_count} words: \
                            \nEmail: {email_body}\
                            \nInsurance Type:\
                            \nIntended Action:"
                }
              ]      
  tokens_used, finish_reason, email_intent = aoai.getChatCompletion(the_engine=cli_engine, the_messages=my_prompt)
  print(f"\tTokens: {tokens_used}")
  print(f"\tFinish Reason: {finish_reason}")
  print(f"\tIntent: {email_intent}")

  #  Classify with given categories
  email_category_list = [
                        "Quote For Real Estate Commercial", 
                        "Quote for Worker Compensation",
                        "Quote for Technology Company",
                        "Quote for Home Building Material Company",
                        "Quote for Workman Compensation",
                        "Quote for Commercial Transport Fleet",
                        "Quote for Auto Insurance",
                        " Other"
                        ]
  intents = ','.join(email_category_list)
  print(f"---Find Intent in the email from Categories: {intents}")
  my_prompt = [
              {
                "role": "user", 
                "content": f"Find the intent in the following email into 1 of the following categories: \
                            {intents} \
                            \nEmail: {email_body}\
                            \n\tIntent:"
                }
              ]      
  tokens_used, finish_reason, email_intent = aoai.getChatCompletion(the_engine=cli_engine, the_messages=my_prompt)
  print(f"\tTokens: {tokens_used}")
  print(f"\tFinish Reason: {finish_reason}")
  print(f"\tIntent: {email_intent}")

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
                            os.environ['OPENAI_API_ENDPOINT']
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
  global cli_email_summary_word_count
  status, cli_email_summary_word_count = getArgumentValue(
                            '-s', '--summary-word-count', 
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS,
                            '5'
                            )
  if cli_email_summary_word_count: print(f'Max word count for summary: {cli_email_summary_word_count}')
  global cli_intent_word_count
  status, cli_intent_word_count = getArgumentValue(
                            '-i', '--intent-word-count', 
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS,
                            '10'
                            )
  if cli_intent_word_count: print(f'Max word count for intent: {cli_intent_word_count}')
  global cli_email_filepath
  currentFolder = os.getcwd()
  status, cli_email_filepath = getArgumentValue(
                            '-f', '--email-filepath', 
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS,
                            f'{currentFolder}\\AutoInsuranceQuote.msg'
                            )
  if cli_email_filepath: print(f'Email filepath: {cli_email_filepath}')    

main()