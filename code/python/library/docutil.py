import os
import json
import aitr
from fpdf import FPDF

def generateEnglishPDFDocsFromJson(json_file, output_folder, translator_client):        
    with open(json_file, 'r', encoding='utf-8') as file:
            input_data = json.load(file)
    for item in input_data:
        email_file_name = item['FileName']
        email_body = item['EmailBody']
        email_body_in_english = aitr.translate(
                                        translator=translator_client, 
                                        content=email_body, 
                                        to_lang='en')
        output_file = f'{output_folder}\{email_file_name}'
        createPDF(file_path=output_file, content=email_body_in_english)
    return

def generateCategoryFileList(json_file, output_folder, docs_subfolder, categories):
    with open(json_file, 'r', encoding='utf-8') as file:
            input_data = json.load(file)
    for item in input_data:
        email_file_name = item['FileName']
        for category in categories:
            if item[category] == True:
                f = open(f'{output_folder}\{category}.jsonl', "a")
                file_path = f'/{docs_subfolder}/{email_file_name}'
                f.write('{"file":"' + file_path + '"}\n')
                f.close
    return
    
def createPDF(file_path, content):
    pdf = FPDF()
    pdf.compress = False
    pdf.accept_page_break()
    pdf.set_margins(left=30.0, top=30.0, right=-1)
    pdf.add_page()
    pdf.add_font(family='arial', fname=r'c:\WINDOWS\Fonts\arial.ttf', uni=True)
    pdf.set_font(family='arial', size=10)
    pdf.write(5,content)
    pdf.output(file_path, 'F')
    pdf.close()
    return                

