# Deployment Instructions

> **Note:** Ensure you are running PowerShell 7+ for the following commands.
> **Prerequisites:**
> - [Azure CLI must be installed](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli-windows?pivots=winget).
> - [Azure Functions Core Tools must be installed](https://learn.microsoft.com/en-us/azure/azure-functions/functions-run-local?tabs=windows%2Cisolated-process%2Cnode-v4%2Cpython-v2%2Chttp-trigger%2Ccontainer-apps&pivots=programming-language-powershell#install-the-azure-functions-core-tools).
> - [Maven must be installed](https://maven.apache.org/install.html).
> - Ensure you have the necessary permissions to create resources in the specified Azure subscription and resource group.

1. Log in to Azure:
    > Replace `<your-tenant-id>` with the ID or domain name of your Azure Active Directory tenant.
    ```powershell
    az login --tenant <your-tenant-id>
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

4. Extract Variables from Output:
    ```powershell
    $deploymentData = $deploymentOutput | ConvertFrom-Json
    $functionAppName = $deploymentData.properties.outputs.pythonFunctionName.value
    $logicAppName = $deploymentData.properties.outputs.logicAppName.value
    $emailStorageAccountName = $deploymentData.properties.outputs.emailStorageAccountName.value
    $subscriptionId = $deploymentData.properties.outputs.subscriptionId.value
    ```

5. Deploy additional components:
    ```powershell
    # Create Video Analyzer
    . ./scripts/createVideoAnalyzer.ps1
    # Deploy Function App
    . ./scripts/deployFunctions.ps1 -FunctionAppName $functionAppName
    
    # Deploy Logic App
    . ./scripts/deployLogicAppZip.ps1 -SubscriptionId $subscriptionId -ResourceGroup $resourceGroup -FunctionAppName $functionAppName -LogicAppName $logicAppName -EmailStorageAccountName $emailStorageAccountName

    # Upload DocIntel Model Data
    . ./scripts/uploadDocIntelModelData.ps1

    # Deploy Web Application
    . ./scripts/deployWebApp.ps1
    ```

6. Create Models in DocIntel

7. Reestablish LogicApp Connections

8. Setup Web Application App Registration