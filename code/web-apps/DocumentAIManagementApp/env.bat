@echo off

if "%~1"=="" (
    echo First Argument Missing.
    echo Command -^> %0 ^<dev/prod^>
    GOTO :EOF
)
@echo on
if "%~1"=="dev" (
    set SUFFIX=DEV
    echo Setting up %SUFFIX% environment variables
    set BLOB_STORE_SAS_TOKEN=%BLOB_STORE_SAS_TOKEN_DEV%
	set COSMOSDB_API_KEY=%COSMOSDB_API_KEY_DEV%
	set COSMOSDB_URI=%COSMOSDB_URI_DEV%
	set DOCAI_EMAIL_RECEIVER_ADDRESS=%DOCAI_EMAIL_RECEIVER_ADDRESS_DEV%
    
) else if "%~1"=="prod" (
    set SUFFIX=PROD
    echo Setting up %SUFFIX% environment variables
    set BLOB_STORE_SAS_TOKEN=%BLOB_STORE_SAS_TOKEN_PROD%
	set COSMOSDB_API_KEY=%COSMOSDB_API_KEY_PROD%
	set COSMOSDB_URI=%COSMOSDB_URI_PROD%
	set DOCAI_EMAIL_RECEIVER_ADDRESS=%DOCAI_EMAIL_RECEIVER_ADDRESS_PROD%
    
) else (
    echo Command -^> %0 ^<dev/prod^>
    GOTO :EOF
)

set COSMOSDB_DATABASE=DocAIDatabase
set COSMOSDB_CONTAINER=EmailExtracts
set COSMOSDB_CONTAINER_DEMOS=DocAIDemos

set COGNITIVE_SERVICE_KEY=%AI_VISION_SERVICE_API_KEY%
set COGNITIVE_SERVICE_ENDPOINT=%AI_VISION_SERVICE_ENDPOINT%

set ALL_AI_SERVICES_API_KEY=%ALL_AI_SERVICES_API_KEY%
set ALL_AI_SERVICES_ENDPOINT=%ALL_AI_SERVICES_ENDPOINT%
set BING_KEY=%BING_API_KEY%
set BING_QUERY_COUNT=5

set OPENAI_API_ENDPOINT=%OPENAI_API_ENDPOINT%
set OPENAI_API_KEY=%OPENAI_API_KEY%
set OPENAI_API_ENGINE=%OPENAI_API_ENGINE%
set OPENAI_API_VERSION=%OPENAI_API_VERSION%

set OPENAI_MULTI_MODAL_API_ENDPOINT=%OPENAI_MULTI_MODAL_API_ENDPOINT%
set OPENAI_MULTI_MODAL_API_KEY=%OPENAI_MULTI_MODAL_API_KEY%
set OPENAI_OMNI_API_ENGINE=%OPENAI_OMNI_API_ENGINE%
set OPENAI_OMNI_API_VERSION=%OPENAI_OMNI_API_VERSION%
set OPENAI_VISION_API_ENGINE=%OPENAI_VISION_API_ENGINE%
set OPENAI_VISION_API_VERSION=%OPENAI_VISION_API_VERSION%
set OPENAI_VISION_VIDEO_INDEX=%OPENAI_VISION_VIDEO_INDEX%
set AI_VIDEO_API_VERSION=%AI_VIDEO_API_VERSION%

set DOCAI_APP_TENANT_ID=%DOCAI_APP_TENANT_ID%
set DOCAI_APP_CLIENT_ID=%DOCAI_APP_CLIENT_ID%
set DOCAI_APP_CLIENT_SECRET=%DOCAI_APP_CLIENT_SECRET%

set DOCAI_EMAIL_SENDER_ADDRESS=%DOCAI_EMAIL_SENDER_ADDRESS%
set DOCAI_EMAIL_SUBJECT_PREFIX=%DOCAI_EMAIL_SUBJECT_PREFIX%
set DOCAI_DEMO_USERS=%DOCAI_DEMO_USERS%

set GRAPH_API_CLIENT_ID=%GRAPH_API_CLIENT_ID%
set GRAPH_API_CLIENT_SECRET=%GRAPH_API_CLIENT_SECRET%
set GRAPH_API_TENANT_ID=%GRAPH_API_TENANT_ID%

:EOF