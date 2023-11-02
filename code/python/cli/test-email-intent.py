import os, sys
import aoai
import outlook

def main():
  cli_endpoint = sys.argv[1]
  cli_engine = sys.argv[2]
  cli_version = sys.argv[3]
  cli_email_summary_in_n_words = sys.argv[4]
  cli_intent_in_n_words = sys.argv[5]
  cli_email_filepath = sys.argv[6]

  email_subject, email_body, attachment_list = outlook.getEmailSubjectBodyAttachmentList(
                                                          cli_email_filepath
                                                      )
  #print(f"----------Body: [{email_body}]------------")
  aoai.setupOpenai(cli_endpoint, cli_version)

  # Provide Summary of Email Body
  print(f'---Email Subject: {email_subject}')
  my_prompt = [
              {
                "role": "user", 
                "content": f"Find the summary of the following email in {cli_email_summary_in_n_words} words: \
                            \nEmail: {email_body}\
                            \nSummary:"
                }
              ]      
  tokens_used, finish_reason, email_summary = aoai.getChatCompletion(the_engine=cli_engine, the_messages=my_prompt)
  print(f"\tTokens: {tokens_used}")
  print(f"\tFinish Reason: {finish_reason}")
  print(f"\tSummary: {email_summary}")

  # Get intent without grounding with an option list
  print(f"---Find Intent in this email strictly in {cli_intent_in_n_words} words")
  my_prompt = [
              {
                "role": "user", 
                "content": f"Find Insurnace Type and Intended Action in the following email in {cli_intent_in_n_words} words: \
                            \nEmail: {email_body}\
                            \nInsurnace Type:\
                            \n\tIntended Action:"
                }
              ]      
  tokens_used, finish_reason, email_intent = aoai.getChatCompletion(the_engine=cli_engine, the_messages=my_prompt)
  print(f"\tTokens: {tokens_used}")
  print(f"\tFinish Reason: {finish_reason}")
  print(f"\tIntent: {email_intent}")

  # Classify with given categories
  email_category_list = [
                        "Quote For Real Estate Commercial", 
                        "Quote for Worker Compensation",
                        "Quote for Technology Company",
                        "Quote for Home Building Material Company",
                        "Quote for Workman Compensation",
                        "Quote for Commercial Transport Fleet",
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
                            \nIntent:"
                }
              ]      
  tokens_used, finish_reason, email_intent = aoai.getChatCompletion(the_engine=cli_engine, the_messages=my_prompt)
  print(f"\tTokens: {tokens_used}")
  print(f"\tFinish Reason: {finish_reason}")
  print(f"\tIntent: {email_intent}")

main()