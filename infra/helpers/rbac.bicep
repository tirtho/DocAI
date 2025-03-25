param pythonFunctionPrincipalId string
param emailStorageAccountName string
param docIntelStorageAccountName string
param azureOpenAIName string
param azureOpenAIVisionName string
param openAIPrincipalId string
param documentIntelligencePrincipalId string
param logicAppPrincipalId string

@description('This is the built-in Cognitive Services OpenAI Contributor role. See https://docs.microsoft.com/azure/role-based-access-control/built-in-roles')
resource CognitiveServicesOpenAIContributorRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  name: 'a001fd3d-188f-4b5d-821b-7da978bf7442'
}

resource StorageBlobDataContributorRoleDefinition 'Microsoft.Authorization/roleDefinitions@2022-04-01' existing = {
  scope: subscription()
  name: 'ba92f5b4-2d11-453d-a403-e96b0029c9fe'
}

resource openAIAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' existing = {
  name: azureOpenAIName
}

resource openAIVisionAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' existing = {
  name: azureOpenAIVisionName
}

resource emailStorageAccount 'Microsoft.Storage/storageAccounts@2023-05-01' existing = {
  name: emailStorageAccountName
}

resource docIntelStorageAccount 'Microsoft.Storage/storageAccounts@2023-05-01' existing = {
  name: docIntelStorageAccountName
}

// Role assignment to give the Function App access to OpenAI Contributor role
resource CognitiveServicesOpenAIContributorRoleAssignment 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(openAIAccount.id, pythonFunctionPrincipalId, CognitiveServicesOpenAIContributorRoleDefinition.id)
  scope: openAIAccount
  properties: {
    roleDefinitionId: CognitiveServicesOpenAIContributorRoleDefinition.id
    principalId: pythonFunctionPrincipalId
    principalType: 'ServicePrincipal'
  }
}

// Role assignment to give the Function App access to OpenAI Contributor role
resource CognitiveServicesOpenAIContributorRoleAssignment_Vision 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(openAIVisionAccount.id, pythonFunctionPrincipalId, CognitiveServicesOpenAIContributorRoleDefinition.id)
  scope: openAIVisionAccount
  properties: {
    roleDefinitionId: CognitiveServicesOpenAIContributorRoleDefinition.id
    principalId: pythonFunctionPrincipalId
    principalType: 'ServicePrincipal'
  }
}

resource StorageBlobDataContributorRoleAssignment 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(emailStorageAccount.id, openAIPrincipalId, StorageBlobDataContributorRoleDefinition.id)
  scope: emailStorageAccount
  properties: {
    roleDefinitionId: StorageBlobDataContributorRoleDefinition.id
    principalId: openAIPrincipalId
    principalType: 'ServicePrincipal'
  }
}

resource StorageBlobDataContributorRoleAssignment2 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(emailStorageAccount.id, documentIntelligencePrincipalId, StorageBlobDataContributorRoleDefinition.id)
  scope: emailStorageAccount
  properties: {
    roleDefinitionId: StorageBlobDataContributorRoleDefinition.id
    principalId: documentIntelligencePrincipalId
    principalType: 'ServicePrincipal'
  }
}

resource StorageBlobDataContributorRoleAssignment2b 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(docIntelStorageAccount.id, documentIntelligencePrincipalId, StorageBlobDataContributorRoleDefinition.id)
  scope: docIntelStorageAccount
  properties: {
    roleDefinitionId: StorageBlobDataContributorRoleDefinition.id
    principalId: documentIntelligencePrincipalId
    principalType: 'ServicePrincipal'
  }
}

resource StorageBlobDataContributorRoleAssignment3 'Microsoft.Authorization/roleAssignments@2020-04-01-preview' = {
  name: guid(emailStorageAccount.id, logicAppPrincipalId, StorageBlobDataContributorRoleDefinition.id)
  scope: emailStorageAccount
  properties: {
    roleDefinitionId: StorageBlobDataContributorRoleDefinition.id
    principalId: logicAppPrincipalId
    principalType: 'ServicePrincipal'
  }
}
