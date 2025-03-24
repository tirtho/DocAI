az login --tenant M365x91016947.onmicrosoft.com
$resourceGroup ="docai-bicep"
$location = "westus"
az group create --name $resourceGroup --location $location

$deploymentOutput = az deployment group create --template-file .\main.bicep --parameters .\main.parameters.json --resource-group $resourceGroup --output json

. ./scripts/deployFunctions.ps1
. ./scripts/deployLogicAppZip.ps1
. ./scripts/uploadDocIntelModelData.ps1
. ./scripts/deployWebApp.ps1
<!-- Create Models in DocIntel -->
<!-- Reestabllish LogicApp Connections -->