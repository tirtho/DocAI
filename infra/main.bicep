targetScope = 'resourceGroup'

@description('Application name to be used in components')
param resourceToken string = toLower(uniqueString(subscription().id, resourceGroup().id, resourceGroup().location))

@description('ObjectID of Key Vault Administrator')
param keyVaultAdminObjectId string = deployer().objectId

@description('Override Location for document intelligence resource.')
param documentIntelligenceLocationOverride string = 'eastus'

// Optional parameters to override the default resource naming conventions. Update the main.parameters.json file to provide values. e.g.,:
// "emailStorageAccountName": {
//      "value": "mystorageaccount"
// }

param emailStorageAccountName string = 'sa8emails${resourceToken}'
param docIntelStorageAccountName string = 'sa8docintel${resourceToken}'
param cosmosDBAccountName string = 'docai-dev-cosmosdb-${resourceToken}'
param functionAppName string = 'func-py-${resourceToken}'
param logicAppName string = 'logicapp-${resourceToken}'
param keyVaultName string = 'keyvault-${resourceToken}'
param aiServicesName string = 'docai-ai-non-prod-${resourceToken}'
param documentIntelligenceName string = 'docai-doc-intel-non-prod-${resourceToken}'
param webAppName string = 'web-app-${resourceToken}'
param appRegistrationTenantID string = tenant().tenantId
param tenantDomainName string = ''

// Java Web App Registration
// Update in main.parameters.json
param webAppClientId string = ''
@secure()
param webAppClientSecret string = ''

// Graph API App Registration (Used by Java Web App)
// Update in main.parameters.json
param graphAPIClientID string = ''
@secure()
param graphAPIClientSecret string = ''

module vnet './helpers/vnet.bicep' = {
  name: 'vnet'
}

module logicApp './helpers/logicApp.bicep' = {
  name: 'logicApp'
  params: {
    logicAppName: logicAppName
    resourceToken: resourceToken
  }
}

module dataStorage './helpers/dataStorage.bicep' = {
  name: 'dataStorage'
  params: {
    emailStorageAccountName: emailStorageAccountName
    docIntelStorageAccountName: docIntelStorageAccountName
  }
}

module aiService './helpers/aiService.bicep' = {
  name: 'aiService'
  params: {
    aiServicesName: aiServicesName
    documentIntelligenceName: documentIntelligenceName
    documentIntelligenceLocationOverride: documentIntelligenceLocationOverride
  }
}

module functionDependencies './helpers/function_dependencies.bicep' = {
  name: 'function_dependencies'
  params: {
    resourceToken: resourceToken
  }
}

module functionPython './helpers/function_python.bicep' = {
  name: 'function_python'
  params: {
    functionAppName: functionAppName
    keyVaultName: keyVault.outputs.keyVaultName
    vnetSubnetId: vnet.outputs.subnetId
    storageAccountName: functionDependencies.outputs.storageAccountName
    hostingPlanID: functionDependencies.outputs.hostingPlanID
    applicationInsightsKey: functionDependencies.outputs.applicationInsightsKey
    additionalAppSettings: [
      { name: 'DOCAI_AOAI_API_ENDPOINT', value: aiService.outputs.azureOpenAIEndpoint }
      { name: 'DOCAI_AOAI_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=DOCAI-AOAI-API-KEY)' }
      { name: 'DOCAI_AOAI_API_VERSION', value: '2024-02-15-preview' }
      { name: 'DOCAI_AOAI_DEFAULT_ENGINE', value: 'gpt-4o' }
      { name: 'DOCAI_COSMOSDB_URI__accountEndpoint', value: cosmosDB.outputs.endpoint }
      { name: 'DOCAI_COSMOSDB_URI__credential', value: 'managedidentity' }
      { name: 'DOCAI_CU_API_ENDPOINT', value: aiService.outputs.contentUnderstandingEndpoint }
      { name: 'DOCAI_CU_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=DOCAI-CU-API-KEY)' }
      { name: 'DOCAI_CU_API_VERSION', value: '2024-12-01-preview' }
      { name: 'DOCAI_CU_VIDEO_ANALYZER_ID', value: 'docai-video-analyzer' }
      { name: 'DOCAI_DOCINTEL_API_ENDPOINT', value: aiService.outputs.documentIntelligenceEndpoint }
      { name: 'DOCAI_DOCINTEL_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=DOCAI-DOCINTEL-API-KEY)' }
      { name: 'DOCAI_DOCINTEL_CLASSIFIER_ID', value: 'docai-classifier-v1' }
      { name: 'DOCAI_DOCINTEL_EXTRACTION_MODEL_CLASS_MAP', value: '[{\'unknown\':\'unknown\'},{\'auto-insurance-claim\':\'autoInsuranceClaimExtraction-v1\'},{\'commercial-insurance-application\':\'commercialInsuranceApplicationExtraction-v1\'},{\'workers-compensation-application\':\'workersCompensationApplicationExtraction-v1\'}]' }
      { name: 'DOCAI_DOCUMENT_CONFIDENCE_THRESHOLD', value: '0.7' }
    ]
  }
}

module keyVault './helpers/keyVault.bicep' = {
  name: 'keyVault'
  params: {
    keyVaultName: keyVaultName
    userPrincipalId: keyVaultAdminObjectId
    vnetSubnetId: vnet.outputs.subnetId
  }
}

module keyVaultSecrets './helpers/keyVaultSecrets.bicep' = {
  name: 'keyVaultSecrets'
  params: {
    keyVaultName: keyVault.outputs.keyVaultName
    azureOpenAIName: aiService.outputs.azureOpenAIName
    contentUnderstandingName: aiService.outputs.contentUnderstandingName
    documentIntelligenceName: aiService.outputs.documentIntelligenceName
    emailStorageAccountName: dataStorage.outputs.emailStorageAccountName
    emailStorageContainerName: dataStorage.outputs.emailContainerName
    cosmosDBAccountName: cosmosDB.outputs.cosmosDBAccountName
  }
}

module keyVaultSecrets_External './helpers/keyVaultSecrets_External.bicep' = {
  name: 'keyVaultSecrets_External'
  params: {
    keyVaultName: keyVault.outputs.keyVaultName
    webAppClientSecret: webAppClientSecret
    graphAPIClientSecret: graphAPIClientSecret
  }
}

module rbac './helpers/rbac.bicep' = {
  name: 'rbac'
  params: {
    pythonFunctionPrincipalId: functionPython.outputs.pythonFunctionPrincipalId
    openAIPrincipalId: aiService.outputs.openAIPrincipalId
    documentIntelligencePrincipalId: aiService.outputs.documentIntelligencePrincipalId
    logicAppPrincipalId: logicApp.outputs.logicAppPrincipalId
    emailStorageAccountName: dataStorage.outputs.emailStorageAccountName
    docIntelStorageAccountName: dataStorage.outputs.docIntelStorageAccountName
    azureOpenAIName: aiService.outputs.azureOpenAIName
  }
}

module cosmosDB './helpers/cosmosDB.bicep' = {
  name: 'cosmosDB'
  params: {
    cosmosDBAccountName: cosmosDBAccountName
  }
}

module cosmosRBAC './helpers/cosmosRBAC.bicep' = {
  name: 'cosmosRBAC'
  params: {
    databaseAccountName: cosmosDB.outputs.cosmosDBAccountName
    principalId: functionPython.outputs.pythonFunctionPrincipalId
  }
}

module cosmosRBACJava './helpers/cosmosRBAC.bicep' = {
  name: 'cosmosRBACJava'
  params: {
    databaseAccountName: cosmosDB.outputs.cosmosDBAccountName
    principalId: webApp.outputs.webAppJavaPrincipalId
  }
}

module webApp './helpers/webApp.bicep' = {
  name: 'webApp'
  params: {
    appServiceAppName: webAppName
    keyVaultName: keyVault.outputs.keyVaultName
    vnetSubnetId: vnet.outputs.subnetId
    additionalAppSettings: [
      { name: 'BLOB_STORE_SAS_TOKEN', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=BLOB-STORE-SAS-TOKEN)' }
      { name: 'DOCAI_AOAI_API_ENDPOINT', value: aiService.outputs.azureOpenAIEndpoint }
      { name: 'DOCAI_AOAI_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=DOCAI-AOAI-API-KEY)' }
      { name: 'DOCAI_AOAI_API_VERSION', value: '2024-02-15-preview' }
      { name: 'DOCAI_AOAI_DEFAULT_ENGINE', value: 'gpt-4o' }
      { name: 'DOCAI_APP_CLIENT_ID', value: webAppClientId }
      { name: 'DOCAI_APP_CLIENT_SECRET', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=DOCAI-APP-CLIENT-SECRET)'}
      { name: 'DOCAI_APP_TENANT_ID', value: appRegistrationTenantID }
      { name: 'DOCAI_BING_AI_API_ENDPOINT', value: 'NOTUSED' } // 2025-03-17 - Not Currently Used      
      { name: 'DOCAI_BING_AI_API_KEY', value: 'NOTUSED' } // 2025-03-17 - Not Currently Used
      { name: 'DOCAI_BING_API_KEY', value: 'NOTUSED' } // 2025-03-17 - Not Currently Used                
      { name: 'DOCAI_BING_QUERY_COUNT', value: '5' }
      { name: 'DOCAI_COSMOSDB_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=COSMOSDB-API-KEY)' }
      { name: 'DOCAI_COSMOSDB_CONTAINER', value: 'EmailExtracts' }
      { name: 'DOCAI_COSMOSDB_CONTAINER_DEMOS', value: 'DocAIDemos' }
      { name: 'DOCAI_COSMOSDB_DATABASE', value: 'DocAIDatabase' }
      { name: 'DOCAI_COSMOSDB_URI', value: cosmosDB.outputs.endpoint }
      { name: 'DOCAI_CU_API_ENDPOINT', value: aiService.outputs.contentUnderstandingEndpoint }
      { name: 'DOCAI_CU_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=DOCAI-CU-API-KEY)' }
      { name: 'DOCAI_CU_API_VERSION', value: '2024-12-01-preview' }
      { name: 'DOCAI_CU_VIDEO_ANALYZER_ID', value: 'docai-video-analyzer' }
      { name: 'DOCAI_DEMO_USERS', value: '*@${tenantDomainName}' } // Comma separated list
      { name: 'DOCAI_EMAIL_RECEIVER_ADDRESS', value: 'docai@${tenantDomainName}' }
      { name: 'DOCAI_EMAIL_SENDER_ADDRESS', value: 'fsi-demo@${tenantDomainName}' }
      { name: 'DOCAI_EMAIL_SUBJECT_PREFIX', value: 'docai' }
      { name: 'DOCAI_GRAPH_API_CLIENT_ID', value: graphAPIClientID }
      { name: 'DOCAI_GRAPH_API_CLIENT_SECRET', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=GRAPH-API-CLIENT-SECRET)' }
      { name: 'DOCAI_GRAPH_API_TENANT_ID', value: appRegistrationTenantID }
    ]
  }
}

output keyVaultName string = keyVault.outputs.keyVaultName
output emailStorageAccountName string = dataStorage.outputs.emailStorageAccountName
output emailStorageContainerName string = dataStorage.outputs.emailContainerName
output docIntelStorageAccountName string = dataStorage.outputs.docIntelStorageAccountName
output docIntelStorageContainerName string = dataStorage.outputs.docIntelContainerName
output pythonFunctionName string = functionPython.outputs.pythonFunctionName
output webAppName string = webApp.outputs.webAppJavaHostName
output logicAppName string = logicApp.outputs.logicAppName
output openAIName string = aiService.outputs.azureOpenAIName
output subscriptionId string = subscription().subscriptionId
