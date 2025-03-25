# Deployment Instructions

> **Note:** Ensure you are running PowerShell 7+ for the following commands.
> **Prerequisites:**
> - [Azure CLI must be installed](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli-windows?pivots=winget).
> - [Azure Functions Core Tools must be installed](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local?tabs=windows%2Cisolated-process%2Cnode-v4%2Cpython-v2%2Chttp-trigger%2Ccontainer-apps&pivots=programming-language-powershell#install-the-azure-functions-core-tools).
> - [Maven must be installed](https://maven.apache.org/install.html).
> - Ensure you have the necessary permissions to create resources in the specified Azure subscription and resource group.

1. Log in to Azure:
    ```powershell
    az login --tenant M365x91016947.onmicrosoft.com
    ```

2. Set up resource group and location:
    ```powershell
    $resourceGroup = "docai-bicep"
    $location = "westus"
    az group create --name $resourceGroup --location $location
    ```

3. Deploy resources using Bicep template:
    ```powershell
    $deploymentOutput = az deployment group create --template-file .\main.bicep --parameters .\main.parameters.json --resource-group $resourceGroup --output json
    ```

4. Deploy additional components:
    ```powershell
    . ./scripts/deployFunctions.ps1
    . ./scripts/deployLogicAppZip.ps1
    . ./scripts/uploadDocIntelModelData.ps1
    . ./scripts/deployWebApp.ps1
    ```

5. Create Models in DocIntel

6. Reestablish LogicApp Connections

7. Setup Web Application App Registration