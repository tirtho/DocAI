# Check if $deploymentOutput is set
if (-not $deploymentOutput) {
    Write-Output "Error: \$deploymentOutput is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

$deploymentData = $deploymentOutput | ConvertFrom-Json

# Variables
$openAIName = $deploymentData.properties.outputs.openAIName.value

Write-Output "Obtaining API Key for $openAIName"
$accountKey = az cognitiveservices account keys list --resource-group $resourceGroup --name $openAIName --query "key1" --output tsv
$endpoints = az cognitiveservices account show --resource-group $resourceGroup --name $openAIName --query "properties.endpoints" | ConvertFrom-Json
$endpoint = $endpoints."Content Understanding" # Get the endpoint for "Content Understanding" service

if (-not $accountKey) {
    Write-Output "Error: Cognitive Services account key not found. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

if (-not $endpoint) {
    Write-Output "Error: Endpoint not found. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

# :: Execute the curl command
# curl -i -X PUT "%endpoint%/contentunderstanding/analyzers/%analyzerId%?api-version=2024-12-01-preview" ^
#   -H "Ocp-Apim-Subscription-Key: %apiKey%" ^
#   -H "Content-Type: application/json" ^
#   -d @%jsonFile%

# Define variables
$analyzerId = "12345"  # Replace with your analyzer ID
$jsonFile = Join-Path -Path $PSScriptRoot -ChildPath "..\..\code\curl\content-understanding\docai-video-analyzer.json"

# Read the JSON file content
$jsonContent = Get-Content -Path $jsonFile -Raw

# Define the API URL
$apiUrl = "$endpoint/contentunderstanding/analyzers/${analyzerId}?api-version=2024-12-01-preview"

# Define headers
$headers = @{
    "Ocp-Apim-Subscription-Key" = $accountKey
    "Content-Type"              = "application/json"
}

# Make the PUT request
$response = Invoke-RestMethod -Uri $apiUrl -Method Put -Headers $headers -Body $jsonContent

$accountKey = $null
$headers = $null