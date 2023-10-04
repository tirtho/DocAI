import os, sys
import aoai
import classifyChat

def main():
  cli_endpoint = sys.argv[1]
  cli_engine = sys.argv[2]
  cli_version = sys.argv[3]
  cli_message = sys.argv[4]
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
  tokens_used, classified_category = aoai.getChatCompletion(the_engine=cli_engine, the_messages=my_prompt)
  #print(f"Logprobs: {log_probs}")
  print(f"Tokens: {tokens_used}")
  print(f"Category: {classified_category}")

main()