param location string = resourceGroup().location
param keyVaultName string
param userPrincipalId string
param vnetSubnetId string

param allowedIpAddress string = ''
var keyVaultBehindVNet = 'true'

var networkRuleSetBehindVNet = {
  defaultAction: 'deny'
  virtualNetworkRules: [
    {
      id: vnetSubnetId
    }
  ]
  ipRules: empty(allowedIpAddress)
    ? []
    : [
        {
          value: allowedIpAddress
        }
      ]
}

@description('This is the built-in Key Vault Administrator role. See https://docs.microsoft.com/azure/role-based-access-control/built-in-roles')
resource KeyVaultAdministratorRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  name: '00482a5a-887f-4fb3-b363-3b7fe8e74483'
}

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: keyVaultName
  location: location
  properties: {
    enabledForTemplateDeployment: true
    enableRbacAuthorization: true
    tenantId: tenant().tenantId
    accessPolicies: []
    sku: {
      name: 'standard'
      family: 'A'
    }
    networkAcls: ((keyVaultBehindVNet == 'true') ? networkRuleSetBehindVNet : null)
  }
}

resource roleAssignment 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(keyVault.id, userPrincipalId, KeyVaultAdministratorRoleDefinition.id)
  scope: keyVault
  properties: {
    roleDefinitionId: KeyVaultAdministratorRoleDefinition.id // Key Vault Administrator Role
    principalId: userPrincipalId
    principalType: 'User'
  }
}

resource keyVaultSecret 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'COGNITIVE-SERVICE-KEY'
  properties: {
    value: 'MyVerySecretValue'
  }
}

output keyVaultName string = keyVault.name
