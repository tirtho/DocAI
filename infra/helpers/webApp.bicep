param location string = resourceGroup().location
param appServiceAppName string
param keyVaultName string
param vnetSubnetId string
param additionalAppSettings array = []
var hostingPlanName = 'asp-web'

var functionPlanOS = 'Linux'
var isReserved = ((functionPlanOS == 'Linux') ? true : false)

resource appServicePlanWebApp 'Microsoft.Web/serverfarms@2023-12-01' = {
  name: hostingPlanName
  location: location
  sku: {
    name: 'P1v2'
    tier: 'PremiumV2'
  }
  kind: 'linux'
  properties: {
    reserved: isReserved
  }
}

var coreAppSettings = [
  { name: 'AAAAAAAAAA_TEST_AAAAAAAAA', value: 'DEPLOYED BY BICEP' }
]

var webAppAppSettings = length(additionalAppSettings) == 0
  ? coreAppSettings
  : concat(coreAppSettings, additionalAppSettings)

resource webAppJava 'Microsoft.Web/sites@2023-12-01' = {
  name: appServiceAppName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  kind: 'app,linux'
  properties: {
    serverFarmId: appServicePlanWebApp.id
    reserved: isReserved
    siteConfig: {
      linuxFxVersion: 'JAVA|17-java17'
      appSettings: webAppAppSettings
    }
  }
}

resource webAppJava_virtualNetwork 'Microsoft.Web/sites/networkConfig@2022-03-01' = {
  parent: webAppJava
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
  name: guid(keyVault.id, webAppJava.id, KeyVaultSecretsUserRoleDefinition.id)
  scope: keyVault
  properties: {
    roleDefinitionId: KeyVaultSecretsUserRoleDefinition.id // Key Vault Secrets User Role
    principalId: webAppJava.identity.principalId
    principalType: 'ServicePrincipal'
  }
}

output webAppJavaHostName string = webAppJava.properties.defaultHostName
output webAppJavaPrincipalId string = webAppJava.identity.principalId
