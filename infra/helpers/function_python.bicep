param location string = resourceGroup().location
param functionAppName string
param keyVaultName string
param vnetSubnetId string
param storageAccountName string
param hostingPlanID string
param applicationInsightsKey string
param functionWorkerRuntime string = 'python'
param functionPlanOS string = 'Linux'
param linuxFxVersion string = 'Python|3.11'
param additionalAppSettings array = []

var isReserved = ((functionPlanOS == 'Linux') ? true : false)

resource storageAccount 'Microsoft.Storage/storageAccounts@2022-05-01' existing = {
  name: storageAccountName
}

var coreAppSettings = [
  {
    name: 'APPINSIGHTS_INSTRUMENTATIONKEY'
    value: applicationInsightsKey
  }
  {
    name: 'AzureWebJobsStorage'
    value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccountName};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storageAccount.listKeys().keys[0].value}'
  }
  {
    name: 'WEBSITE_CONTENTAZUREFILECONNECTIONSTRING'
    value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccountName};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storageAccount.listKeys().keys[0].value};'
  }
  {
    name: 'WEBSITE_CONTENTSHARE'
    value: toLower(functionAppName)
  }
  {
    name: 'FUNCTIONS_EXTENSION_VERSION'
    value: '~4'
  }
  {
    name: 'FUNCTIONS_WORKER_RUNTIME'
    value: functionWorkerRuntime
  }
]

var pythonAppSettings = length(additionalAppSettings) == 0
  ? coreAppSettings
  : concat(coreAppSettings, additionalAppSettings)

// Python Function App
resource pythonFunctionApp 'Microsoft.Web/sites@2022-03-01' = {
  name: functionAppName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  kind: (isReserved ? 'functionapp,linux' : 'functionapp')
  properties: {
    reserved: isReserved
    serverFarmId: hostingPlanID
    siteConfig: {
      linuxFxVersion: (isReserved ? linuxFxVersion : null)
      appSettings: pythonAppSettings
    }
  }
}

resource functionAppName_virtualNetwork 'Microsoft.Web/sites/networkConfig@2022-03-01' = {
  parent: pythonFunctionApp
  name: 'virtualNetwork'
  properties: {
    subnetResourceId: vnetSubnetId
    swiftSupported: true
  }
}

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' existing = {
  name: keyVaultName
}

@description('This is the built-in Key Vault Administrator role. See https://docs.microsoft.com/azure/role-based-access-control/built-in-roles')
resource KeyVaultSecretsUserRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  name: '4633458b-17de-408a-b874-0445c86b69e6'
}

// Role assignment to give the Function App access to Key Vault secrets
resource keyVaultSecretsUserRoleAssignment 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(keyVault.id, pythonFunctionApp.id, KeyVaultSecretsUserRoleDefinition.id)
  scope: keyVault
  properties: {
    roleDefinitionId: KeyVaultSecretsUserRoleDefinition.id // Key Vault Secrets User Role
    principalId: pythonFunctionApp.identity.principalId
    principalType: 'ServicePrincipal'
  }
}

output pythonFunctionName string = pythonFunctionApp.name
output pythonFunctionPrincipalId string = pythonFunctionApp.identity.principalId
