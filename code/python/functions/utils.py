########### utils.py #################
# Functions to read command line arguments
# and read conf parameters from config
# files in .ini format
# Code Tested with Python 3.9
#####################################
import sys, getopt
import configparser

# Get the default config parameter value from the 
# filename passed as configFilePath
def getConfigParameters(configFilePath, paramName):
    config = configparser.ConfigParser()
    config.read(configFilePath)
    return config['DEFAULT'][paramName]

def getThisRunningFileName():
    return sys.argv[0]

# For the command 
# expectedShortOptions has the arg list you expect
#   example expectedShortOptions = "hi:o:"
#   where h is a short option that does not need a value
#   i: and o: are short options that need a value (as they are with a :)
# expectedLongOptions has the long arg list you expect
#   example expectedLongOptions = ["input=", "output=", "help"]
#   where help is the long option that does not need a value
#   'input' and 'output' are long options that need a value (as they are suffixed with '=')
def getCommandLineArgs(expectedShortOptions, expectedLongOptions):
    justArgs = sys.argv[1:]
    return getopt.getopt(justArgs, expectedShortOptions, expectedLongOptions)

# Return the help text
def getHelpText(helpText):
    return "{}\n{}".format(getThisRunningFileName(), helpText)

def needHelp(commandLineShortOptions, commandLineLongOptions, helpText):
    try:
        opts, args = getCommandLineArgs(commandLineShortOptions, commandLineLongOptions)
        # If help was an argument, process help
        for opt, val in opts:
            if opt == "-h" or opt == "--help":
                #print("{}\n{}".format(getThisRunningFileName(), HELP_TEXT))
                print("{}".format(getHelpText(helpText)))
                return True
        return False
    except getopt.GetoptError:
        print("Invalid arguments")
        print("{}".format(getHelpText(helpText)))
        return True

# Returns True/False, ValueOfArgument
def getArgumentValue(shortName, longName, commandLineShortOptions, commandLineLongOptions, defaulValue):
    try:
        opts, args = getCommandLineArgs(commandLineShortOptions, commandLineLongOptions)
        for opt, val in opts:
            if opt in (shortName, longName):
                return True, val
        return False, defaulValue
    except getopt.GetoptError:
        print("Failed to parse argument {}/{}\n".format(shortName, longName))
        return False, defaulValue
# Returns True/False, ValueOfStringAfterAllKnownArgumentsWereParsed
def getNonArgumentTailString(commandLineShortOptions, commandLineLongOptions):
    try:
        opts, args = getCommandLineArgs(commandLineShortOptions, commandLineLongOptions)
        return True, args
    except getopt.GetoptError:
        print("Failed to parse")
        return False, ''

######### Below is an example code on how to get command line arguments #########
# First check if arguments are passed correctly
# if not, print help text
#########
# if needHelp(commandLineShortOptions, commandLineLongOptions, helpText) == True:
#    print("{} - Help information above".format(getThisRunningFileName()))
#    exit()
#########
#########
# Now get the value for a given argument
# Pass either or both short and long name of argument
# If you do not pass short name, and user passes short name
# you will not get the value. Same with long name.
# So it is better to pass both short and long names
# to catch the value for the argument passed in short or long form by user
#########
# iShort = '-n'
# iLong = '--bot-name'
# status, optionVal = getArgumentValue(iShort, iLong, commandLineShortOptions, commandLineLongOptions)
# if (status == True):
#    print("Found {}".format(optionVal))
# else:
#    print("Not found")
#########
# If anything was passed after all the options 
# defined in the commanline options in the top of this file
# that is returned by this call
# status, input = getNonArgumentTailString(commandLineShortOptions, commandLineLongOptions)
# if (status):
#    print("True {}".format(input))
# else:
#    print("False {}".format(input))
