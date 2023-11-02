import win32com.client


def getEmailSubjectBodyAttachmentList(filepath):

    outlook = win32com.client.Dispatch("Outlook.Application").GetNamespace("MAPI")
    msg = outlook.OpenSharedItem(filepath)

    #print(f'Sender: {msg.SenderName}')
    #print(f'Sender Email Address: {msg.SenderEmailAddress}')
    #print(f'Sent On: {msg.SentOn}')
    #print (f'To: {msg.To}')
    #print(f'CC: {msg.CC}')
    #print(f'BCC: {msg.BCC}')
    #print(f'Subject: {msg.Subject}')
    #print(f'Body: {msg.Body}')

    attachments = []
    count_attachments = msg.Attachments.Count
    if count_attachments > 0:
        for item in range(count_attachments):
            attachments.append(msg.Attachments.Item(item + 1).Filename)
            #print(f'Attachment Filename: {msg.Attachments.Item(item + 1).Filename}')

    subject = msg.Subject
    body = msg.Body
    del outlook, msg
    return subject, body, attachments
