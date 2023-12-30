set COSMOS_KEY=%COSMOSDB_API_KEY%
set COSMOS_URI=%COSMOSDB_URI%
set COSMOS_DATABASE=DocAIDatabase
set COSMOS_CONTAINER=EmailExtracts
set COGNITIVE_SERVICE_KEY=%ALL_AI_SERVICES_API_KEY%
set COGNITIVE_SERVICE_ENDPOINT=%ALL_AI_SERVICES_ENDPOINT%
set BING_KEY=%BING_API_KEY%
set BING_QUERY_COUNT=3

copy pom.xml.runLocal pom.xml
az account set --subscription tr-non-prod
echo "FYI - My Github repo is setup with Gethub actions to kickoff a deployment to the Azure App Service upon git commit"
mvn install "-DRESOURCEGROUP_NAME=DocAI" "-DWEBAPP_NAME=tr-docai-ui" "-DREGION=East US" azure-webapp:deploy 
