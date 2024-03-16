import os, sys
from utils import *
import base64
import re

COMMANDLINE_SHORT_OPTIONS = 'hed' 
COMMANDLINE_LONG_OPTIONS = ['help', 'encode', 'decode']
HELP_TEXT = "  -h/--help for help\n\
  -e/--encode Encode the string\n\
  -d/--decode Decode the String\n"

def main():
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
    status = isThisNoValueArgumentPresent(
                                    COMMANDLINE_SHORT_OPTIONS, 
                                    COMMANDLINE_LONG_OPTIONS,
                                    '-e', '--encode'
                )
    actionType = 'n'
    if status:
        print(f'Encoding')
        actionType = 'e'
    else:
        status = isThisNoValueArgumentPresent(
                                    COMMANDLINE_SHORT_OPTIONS, 
                                    COMMANDLINE_LONG_OPTIONS,
                                    '-d', '--decode'
                )
        if status:
            print(f'Decoding')
            actionType = 'd'

    stringData = ''
    inputStatus, stringData = getNonArgumentTailString(
                            COMMANDLINE_SHORT_OPTIONS, 
                            COMMANDLINE_LONG_OPTIONS
                        )
    if inputStatus == True:
        if actionType == 'd':
            decodedBytes = base64.b64decode(str(stringData))
            decodedString = decodedBytes.decode("utf-8")
            #d = re.findall(r"'([^']*)'", decodedString)
            d = decodedString[2:-2]
            print(f'Decoded value:{d}')
        elif actionType == 'e':
            encodedString = base64.b64encode(str(stringData).encode("utf-8"))
            print(f'Encoded value:{encodedString}')
        else:
            print(f'{HELP_TEXT}')
    else:
        print('No string to act upon')
        print(f'{HELP_TEXT}')
        exit()
 
main()
