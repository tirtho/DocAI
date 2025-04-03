# Check if $deploymentOutput is set
if (-not $deploymentOutput) {
    Write-Output "Error: \$deploymentOutput is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

$deploymentData = $deploymentOutput | ConvertFrom-Json

# Variables
$storageAccount = $deploymentData.properties.outputs.docIntelStorageAccountName.value
$storageAccount_containerName = $deploymentData.properties.outputs.docIntelStorageContainerName.value

Write-Output "Obtaining Storage Account Key for $storageAccount"
$accountKey = az storage account keys list --resource-group $resourceGroup --account-name $storageAccount --query "[0].value" --output tsv

if (-not $accountKey) {
    Write-Output "Error: Storage account key not found. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

$filePath = Join-Path -Path $PSScriptRoot -ChildPath "..\..\data\doc-intel-models\"
Write-Output "Uploading Files to $storageAccount from $filePath"
az storage blob upload-batch --destination $storageAccount_containerName --source $filePath --account-name $storageAccount --account-key $accountKey --output none
$accountKey = $null