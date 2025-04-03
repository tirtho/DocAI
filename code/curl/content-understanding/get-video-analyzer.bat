REM Description: 
REM This script gets the analyzer information from the Content Understanding Service  
@echo off

:: Check if the first argument is "help" or "--help"
if "%1"=="help" (
    echo Usage: create-video-analyzer.bat [analyzerId]
    echo Description:
    echo   This script gets a video analyzer in CU.
    echo   Arguments:
    echo     analyzerId - The ID of the analyzer to create/update.
    echo.
    echo Example:
    echo   get-video-analyzer.bat 12345
    exit /b
)

:: Set variables from input arguments
set analyzerId=%1

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

:: Execute the curl command
curl -i -X GET "%endpoint%/contentunderstanding/analyzers/%analyzerId%?api-version=2024-12-01-preview" ^
  -H "Ocp-Apim-Subscription-Key: %apiKey%" ^
  -H "Content-Type: application/json" ^  