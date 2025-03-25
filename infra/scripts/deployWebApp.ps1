# Check if $deploymentOutput is set
if (-not $deploymentOutput) {
    Write-Output "Error: \$deploymentOutput is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

$deploymentData = $deploymentOutput | ConvertFrom-Json

# Parameters
$webAppDir = Join-Path -Path $PSScriptRoot -ChildPath "..\..\code\web-apps\DocumentAIManagementApp"
$webAppName_JavaApp = $deploymentData.properties.outputs.webAppName.value.split('.')[0]

if(-not $webAppName_JavaApp) {
    Write-Output "Error: webAppName_JavaApp is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

# Save the original directory
$originalDir = Get-Location

# Publish Java Web App
Set-Location $webAppDir
mvn -f pom-bicep.xml install "-DRESOURCEGROUP_NAME=$resourceGroup" "-DWEBAPP_NAME=$webAppName_JavaApp" "-DREGION=$location" azure-webapp:deploy

# Restore the original directory
Set-Location $originalDir