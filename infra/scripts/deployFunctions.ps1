# Check if $deploymentOutput is set
if (-not $deploymentOutput) {
    Write-Output "Error: \$deploymentOutput is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

$deploymentData = $deploymentOutput | ConvertFrom-Json

# Parameters
$pythonFunctionDir = Join-Path -Path $PSScriptRoot -ChildPath "..\..\code\python\functions"
$csharpFunctionDir = Join-Path -Path $PSScriptRoot -ChildPath "..\..\code\csharp\functions"
$functionAppName_pythonFunc = $deploymentData.properties.outputs.pythonFunctionName.value
$functionAppName_csharpFunc = $deploymentData.properties.outputs.cSharpFunctionName.value

if(-not $functionAppName_pythonFunc -or -not $functionAppName_csharpFunc) {
    Write-Output "Error: functionAppName_pythonFunc or functionAppName_csharpFunc is not set. Exiting script."
    Exit 1  # Exits with a status code of 1 to indicate an error
}

# Save the original directory
$originalDir = Get-Location

# Publish Python Function App
Set-Location $pythonFunctionDir
func azure functionapp publish $functionAppName_pythonFunc --python

if(-not $?) {
    # Deployment almost always fails once for a refresh AppService.  So, retrying once.
    Write-Host "Deployment Failed...Retrying Once" -f Red
    func azure functionapp publish $functionAppName_pythonFunc --python
}

# Publish C# Function App
Set-Location $csharpFunctionDir
func azure functionapp publish $functionAppName_csharpFunc --dotnet-isolated --dotnet-cli-params "functions.csproj"

# Restore the original directory
Set-Location $originalDir