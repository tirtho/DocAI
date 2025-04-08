@echo off
echo "Make sure you have logged in to Azure and set the right subscription: > az login; > az account set --subscription tr-non-prod"

copy pom.xml.runLocal pom.xml
az login
az account set --subscription "prod"

mvn install "-DRESOURCEGROUP_NAME=docai-prod" "-DWEBAPP_NAME=docai-web-prod"  "-DREGION=East US 2"  azure-webapp:deploy
