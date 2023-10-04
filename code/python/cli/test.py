import os, sys
import aoai
import classify

def main():
  cli_endpoint = sys.argv[1]
  cli_engine = sys.argv[2]
  cli_version = sys.argv[3]
  cli_message = sys.argv[4]
  aoai.setupOpenai(cli_endpoint, cli_version)
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
  tokens_used, classified_category = aoai.getCompletion(the_engine=cli_engine, the_messages=messages)
  print(f"Tokens: {tokens_used}")
  print(f"Category: {classified_category}")

main()