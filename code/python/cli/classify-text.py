import classify
import sys

cli_endpoint = sys.argv[1]
cli_engine = sys.argv[2]
cli_version = sys.argv[3]
cli_message = sys.argv[4]

total_tokens_used, classified_type = classify.classifyText(
                                                            the_user_endpoint=cli_endpoint, 
                                                            the_user_version=cli_version, 
                                                            the_user_engine=cli_engine,
                                                            the_user_prompt=cli_message
                                                           )
print(f'Message: {cli_message}')
if total_tokens_used < 0:
    print("Error classifying user prompt")
else:
    print(f'\nTotal tokens used: {total_tokens_used}\nCategory: {classified_type}')