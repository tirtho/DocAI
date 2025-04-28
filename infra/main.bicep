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
param computerVisionServicesName string = 'docai-ai-vision-non-prod-${resourceToken}'
param azureOpenAIName string = 'docai-aoai-${resourceToken}'
param azureOpenAIVsionName string = 'docai-aoai-vision-${resourceToken}'
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
    computerVisionServicesName: computerVisionServicesName
    azureOpenAIName: azureOpenAIName
    azureOpenAIVsionName: azureOpenAIVsionName
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
      { name: 'AI_VIDEO_API_VERSION', value: '2023-05-01-preview' }
      { name: 'BLOB_STORE_SAS_TOKEN', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=BLOB-STORE-SAS-TOKEN)' }
      { name: 'COGNITIVE_SERVICE_ENDPOINT', value:  aiService.outputs.computerVisionServicesEndpoint }
      { name: 'COGNITIVE_SERVICE_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=COGNITIVE-SERVICE-KEY)' }
      { name: 'CosmosDbConnectionString__accountEndpoint', value: cosmosDB.outputs.endpoint }
      { name: 'CosmosDbConnectionString__credential', value: 'managedidentity' }
      { name: 'DOCUMENT_CLASSIFIER_ID', value: 'docai-classifier-v1' }
      { name: 'DOCUMENT_CONFIDENCE_THRESHOLD', value: '0.7' }  
      { name: 'DOCUMENT_EXTRACTION_MODEL_CLASS_MAP', value: '[{\'unknown\':\'unknown\'},{\'auto-insurance-claim\':\'autoInsuranceClaimExtraction-v1\'},{\'commercial-insurance-application\':\'commercialInsuranceApplicationExtraction-v1\'},{\'workers-compensation-application\':\'workersCompensationApplicationExtraction-v1\'}]' }
      { name: 'FORM_RECOGNIZER_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=FORM-RECOGNIZER-API-KEY)' }      
      { name: 'FORM_RECOGNIZER_ENDPOINT', value: aiService.outputs.documentIntelligenceEndpoint }
      { name: 'OPENAI_API_ENDPOINT', value: aiService.outputs.azureOpenAIEndpoint }
      { name: 'OPENAI_API_ENGINE', value: 'gpt-4o' }
      { name: 'OPENAI_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=OPENAI-API-KEY)' }
      { name: 'OPENAI_API_VERSION', value: '2024-02-15-preview' }
      { name: 'OPENAI_MULTI_MODAL_API_ENDPOINT', value: aiService.outputs.azureOpenAIEndpoint }
      { name: 'OPENAI_MULTI_MODAL_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=OPENAI-MULTI-MODAL-API-KEY)' }
      { name: 'OPENAI_OMNI_API_ENGINE', value: 'gpt-4o' }
      { name: 'OPENAI_OMNI_API_VERSION', value: '2024-02-15-preview' }
      { name: 'OPENAI_VISION_API_ENDPOINT', value: aiService.outputs.azureOpenAIVsionEndpoint }
      { name: 'OPENAI_VISION_API_ENGINE', value: 'gpt-4-vision' }
      { name: 'OPENAI_VISION_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=OPENAI-VISION-API-KEY)' }
      { name: 'OPENAI_VISION_API_VERSION', value: '2023-12-01-preview' }
      { name: 'OPENAI_VISION_VIDEO_INDEX', value: 'tr-docai-video-index' }
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
    computerVisionName: aiService.outputs.computerVisionServicesName
    azureOpenAIName: aiService.outputs.azureOpenAIName
    azureOpenAIVideoName: aiService.outputs.azureOpenAIVsionName
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
    azureOpenAIVisionName: aiService.outputs.azureOpenAIVsionName
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
      { name: 'AI_VIDEO_API_VERSION', value: '2023-05-01-preview' }
      { name: 'ALL_AI_SERVICES_API_KEY', value: 'NOTUSED' } // 2025-03-17 - Not Currently Used
      { name: 'ALL_AI_SERVICES_ENDPOINT', value: 'NOTUSED' } // 2025-03-17 - Not Currently Used
      { name: 'BING_KEY', value: 'NOTUSED' } // 2025-03-17 - Not Currently Used
      { name: 'BING_QUERY_COUNT', value: '5' }
      { name: 'BLOB_STORE_SAS_TOKEN', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=BLOB-STORE-SAS-TOKEN)' }
      { name: 'COGNITIVE_SERVICE_ENDPOINT', value: 'NOTUSED' } // 2025-03-17 - Not Currently Used
      { name: 'COGNITIVE_SERVICE_KEY', value: 'NOTUSED' } // 2025-03-17 - Not Currently Used
      { name: 'COSMOSDB_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=COSMOSDB-API-KEY)' }
      { name: 'COSMOSDB_CONTAINER', value: 'EmailExtracts' }
      { name: 'COSMOSDB_CONTAINER_DEMOS', value: 'DocAIDemos' }
      { name: 'COSMOSDB_DATABASE', value: 'DocAIDatabase' }
      { name: 'COSMOSDB_URI', value: cosmosDB.outputs.endpoint }
      { name: 'DOCAI_APP_CLIENT_ID', value: webAppClientId } 
      { name: 'DOCAI_APP_CLIENT_SECRET', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=DOCAI-APP-CLIENT-SECRET)' } 
      { name: 'DOCAI_APP_TENANT_ID', value: appRegistrationTenantID } 
      { name: 'DOCAI_DEMO_USERS', value: '*@${tenantDomainName}' } // Comma separated list
      { name: 'DOCAI_EMAIL_RECEIVER_ADDRESS', value: 'docai@${tenantDomainName}' } 
      { name: 'DOCAI_EMAIL_SENDER_ADDRESS', value: 'fsi-demo@${tenantDomainName}' } 
      { name: 'DOCAI_EMAIL_SUBJECT_PREFIX', value: 'docai' }
      { name: 'GRAPH_API_CLIENT_ID', value: graphAPIClientID } 
      { name: 'GRAPH_API_CLIENT_SECRET', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=GRAPH-API-CLIENT-SECRET)' } 
      { name: 'GRAPH_API_TENANT_ID', value: appRegistrationTenantID } 
      { name: 'OPENAI_API_ENDPOINT', value: aiService.outputs.azureOpenAIEndpoint }
      { name: 'OPENAI_API_ENGINE', value: 'gpt-4o' }
      { name: 'OPENAI_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=OPENAI-API-KEY)' }
      { name: 'OPENAI_API_VERSION', value: '2024-02-15-preview' }
      { name: 'OPENAI_MULTI_MODAL_API_ENDPOINT', value: aiService.outputs.azureOpenAIEndpoint }
      { name: 'OPENAI_MULTI_MODAL_API_KEY', value: '@Microsoft.KeyVault(VaultName=${keyVaultName};SecretName=OPENAI-MULTI-MODAL-API-KEY)' }
      { name: 'OPENAI_OMNI_API_ENGINE', value: 'gpt-4o' }
      { name: 'OPENAI_OMNI_API_VERSION', value: '2024-02-15-preview' }
      { name: 'OPENAI_VISION_API_ENGINE', value: 'gpt-4o' }
      { name: 'OPENAI_VISION_API_VERSION', value: '2023-12-01-preview' }
      { name: 'OPENAI_VISION_VIDEO_INDEX', value: 'tr-docai-video-index' }
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
output subscriptionId string = subscription().subscriptionId
