@echo off
echo "Make sure you have logged in to Azure and set the right subscription: > az login; > az account set --subscription tr-non-prod"

copy pom.xml.runLocal pom.xml

mvn install "-DRESOURCEGROUP_NAME=DocAI" "-DWEBAPP_NAME=tr-docai-web" "-DREGION=East US" azure-webapp:deploy
