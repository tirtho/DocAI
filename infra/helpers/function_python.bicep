param location string = resourceGroup().location
param resourceToken string
param functionAppName string
param keyVaultName string
param vnetSubnetId string
param functionWorkerRuntime string = 'python'
param functionPlanOS string = 'Linux'
param linuxFxVersion string = 'Python|3.11'
param additionalAppSettings array = []

@description('Name of the Log Analytics workspace to use for the Logic App.')
param logAnalyticsName string

var isReserved = ((functionPlanOS == 'Linux') ? true : false)
var hostingPlanName = 'asp-func'
var applicationInsightsName = 'ai-func'
var storageAccountName = 'safunc${resourceToken}'

param functionAppPlanSku string = 'EP1'

resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2020-08-01' existing = {
  name: logAnalyticsName
}

var coreAppSettings = [
  {
    name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
    value: reference('microsoft.insights/components/${applicationInsightsName}', '2015-05-01').ConnectionString
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
    serverFarmId: hostingPlan.id
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

resource storageAccount 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    supportsHttpsTrafficOnly: true
    minimumTlsVersion: 'TLS1_2'
    defaultToOAuthAuthentication: true
    allowBlobPublicAccess: false
  }
  dependsOn: []
}

resource hostingPlan 'Microsoft.Web/serverfarms@2022-03-01' = {
  name: hostingPlanName
  location: location
  sku: {
    tier: 'ElasticPremium'
    name: functionAppPlanSku
    family: 'EP'
  }
  properties: {
    maximumElasticWorkerCount: 20
    reserved: isReserved
  }
  kind: 'elastic'
}

resource applicationInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: applicationInsightsName
  location: location
  tags: {
    'hidden-link:${resourceId('Microsoft.Web/sites', applicationInsightsName)}': 'Resource'
  }
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: logAnalyticsWorkspace.id
  }
  kind: 'web'
}

output pythonFunctionName string = pythonFunctionApp.name
output pythonFunctionPrincipalId string = pythonFunctionApp.identity.principalId
