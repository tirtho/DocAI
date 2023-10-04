import aoai

def classifyText(the_user_endpoint, the_user_version, the_user_engine, the_user_prompt):
    composed_messages = [{"role":"system","content":"You are an AI assistant that helps people classify information."},
                         {"role":"user","content":"Headline: I want to submit an auto incident claim. Where can I find the how-to information? My account ID is TRI 12345\nCategory:"},
                         {"role":"assistant","content":"claims-new"},
                         {"role":"user","content":"Headline: I have filed a claim a few days back. I need to change the mobile phone number in that claim. How do I do it?\nCategory:"},
                         {"role":"assistant","content":"claims-update"},
                         {"role":"user","content":"Headline: I have filed a claim a few days back. I found that this is a duplicate of a previously filed claim. I need to cancel the latest claim filed.\nCategory:"},
                         {"role":"assistant","content":"claims-cancel"},
                         {"role":"user","content":"Headline: I live in the suburbs of Boston. The entire New England region is a hot tourist spot during Fall.\nCategory:"},
                         {"role":"assistant","content":"general"},
                         {"role":"user","content":f"Headline: {the_user_prompt}\nCategory:"},
                         {"role":"assistant","content":""}
                         ]
    result = aoai.setupOpenai(aoai_endpoint=the_user_endpoint, aoai_version=the_user_version)
    if result < 0:
        return -1, composed_messages
    try:
        return aoai.getCompletion(the_engine=the_user_engine, the_messages=composed_messages)
    except Exception as e:
        # Just print(e) is cleaner and more likely what you want,
        # but if you insist on printing message specifically whenever possible...
        if hasattr(e, 'message'):
            print(e.message)
        else:
            print(e)
        return -2, composed_messages
