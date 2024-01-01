set COSMOS_KEY=%COSMOSDB_API_KEY%
set COSMOS_URI=%COSMOSDB_URI%
set COSMOS_DATABASE=DocAIDatabase
set COSMOS_CONTAINER=EmailExtracts
set COGNITIVE_SERVICE_KEY=%ALL_AI_SERVICES_API_KEY%
set COGNITIVE_SERVICE_ENDPOINT=%ALL_AI_SERVICES_ENDPOINT%
set BING_KEY=%BING_API_KEY%
set BING_QUERY_COUNT=3

copy pom.xml.debugLocal pom.xml

echo "Make sure you have Eclipse is running and setup Eclipse Debug Configuration with Standard socket, localhost, 9090"
echo "After maven starts app locally, run Debug in Eclipse"

mvn package spring-boot:run