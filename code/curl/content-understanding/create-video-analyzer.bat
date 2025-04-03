REM Description: 
REM This script sends a PUT request to a Content Understanding Service  
REM to create or update an analyzer using curl.
@echo off

:: Check if the first argument is "help" or "--help"
if "%1"=="help" (
    echo Usage: create-video-analyzer.bat [analyzerId] [jsonFile]
    echo Description:
    echo   This script creates or updates a video analyzer in CU.
    echo   Arguments:
    echo     analyzerId - The ID of the analyzer to create/update.
    echo     jsonFile   - The path to the JSON file containing the request body.
    echo.
    echo Example:
    echo   create-video-analyzer.bat 12345 docai-video-analyzer.json
    exit /b
)

:: Set variables from input arguments
set analyzerId=%1
set jsonFile=%2

:: Validate arguments and environment variables
set apiKey=%MULTI_AI_SERVICES_API_KEY%
set endpoint=%MULTI_AI_SERVICES_ENDPOINT_CU%
if "%endpoint%"=="" (
    echo Error: MULTI_AI_SERVICES_ENDPOINT_CU environment variable not found.
    exit /b 1
)
if "%apiKey%"=="" (
    echo Error: MULTI_AI_SERVICES_API_KEY environment variable not found.
    exit /b 1
)
if "%analyzerId%"=="" (
    echo Error: Missing analyzerId argument. Use "help" for usage information.
    exit /b 1
)
if "%jsonFile%"=="" (
    echo Error: Missing jsonFile argument. Use "help" for usage information.
    exit /b 1
)

:: Execute the curl command
curl -i -X PUT "%endpoint%/contentunderstanding/analyzers/%analyzerId%?api-version=2024-12-01-preview" ^
  -H "Ocp-Apim-Subscription-Key: %apiKey%" ^
  -H "Content-Type: application/json" ^
  -d @%jsonFile%
  