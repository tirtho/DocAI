# Check if $deploymentOutput is set
if (-not $deploymentOutput) {
    Write-Output "Error: deploymentOutput is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

$deploymentData = $deploymentOutput | ConvertFrom-Json

$subscriptionId = $deploymentData.properties.outputs.subscriptionId.value
$functionAppName_pythonFunc = $deploymentData.properties.outputs.pythonFunctionName.value
$logicAppName = $deploymentData.properties.outputs.logicAppName.value
$emailStorageAccountName = $deploymentData.properties.outputs.emailStorageAccountName.value

if(-not $functionAppName_pythonFunc) {
    Write-Output "Error: functionAppName_pythonFuncis not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

if(-not $logicAppName) {
    Write-Output "Error: logicAppName is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

if(-not $emailStorageAccountName) {
    Write-Output "Error: emailStorageAccountName is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

if(-not $subscriptionId) {
    Write-Output "Error: subscriptionId is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

# Define the folder path containing the JSON files and the output ZIP path
$sourceFolderPath = Join-Path -Path $PSScriptRoot -ChildPath "..\logicAppTemplates\"
$outputZipPath = ".\logicAppDeploy.zip"

# Create a temporary folder to store modified copies of the JSON files
$tempFolder = New-Item -ItemType Directory -Path (Join-Path $env:TEMP "TempJsonFiles")

# Define a dictionary for multiple replacements
$replacements = @{
    "SUBSCRIPTIONPLACEHOLDER" = $subscriptionId
    "RESOURCEGROUPLACEHOLDER" = $resourceGroup
    "PYTHONFUNCPLACEHOLDER" = $functionAppName_pythonFunc
    "STORAGEEMAILSPLACEHOLDER" = $emailStorageAccountName
}

# Copy JSON files to the temporary folder and apply replacements
Get-ChildItem -Path $sourceFolderPath -Filter *.json | ForEach-Object {
    $destinationPath = if ($_.Name -eq "workflow.json") {
        # Place workflow.json inside a subfolder named docAIEmailProcessingLogicApp in the temp directory
        New-Item -ItemType Directory -Path (Join-Path $tempFolder.FullName "docAIEmailProcessingLogicApp") -Force | Out-Null
        Join-Path -Path $tempFolder.FullName -ChildPath "docAIEmailProcessingLogicApp\$($_.Name)"
    }
    else {
        # Place other files directly in the temp folder
        Join-Path -Path $tempFolder.FullName -ChildPath $_.Name
    }
    
    # Read and apply replacements to the content
    $jsonContent = Get-Content -Path $_.FullName -Raw
    foreach ($key in $replacements.Keys) {
        $jsonContent = $jsonContent -replace $key, $replacements[$key]
    }
    
    # Save the modified content to the temp folder at the appropriate path
    Set-Content -Path $destinationPath -Value $jsonContent
}

# Create the ZIP archive from the temporary folder
Compress-Archive -Path (Join-Path $tempFolder.FullName '*') -DestinationPath $outputZipPath -Force

# Clean up the temporary folder
Remove-Item -Path $tempFolder.FullName -Recurse -Force

Write-Output "ZIP file created at $outputZipPath with modified content."

az logicapp deployment source config-zip --name $logicAppName --resource-group $resourceGroup --src $outputZipPath

Remove-Item $outputZipPath

Write-Output "ZIP file removed."