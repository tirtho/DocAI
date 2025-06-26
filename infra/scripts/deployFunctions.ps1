# Parameters for the script
param(
    [Parameter(Mandatory = $true)]
    [string]$FunctionAppName
)

# Path to Python functions
$pythonFunctionDir = Join-Path -Path $PSScriptRoot -ChildPath "..\..\code\python\functions"

# Save the original directory
$originalDir = Get-Location

# Publish Python Function App
Set-Location $pythonFunctionDir
func azure functionapp publish $FunctionAppName --python

if(-not $?) {
    # Deployment almost always fails once for a refresh AppService.  So, retrying once.
    Write-Host "Deployment Failed...Retrying Once" -f Red
    func azure functionapp publish $FunctionAppName --python
}

# Restore the original directory
Set-Location $originalDir