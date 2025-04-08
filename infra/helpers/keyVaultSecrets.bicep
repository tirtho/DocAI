param baseTime string = utcNow('u')

param keyVaultName string
param computerVisionName string
param documentIntelligenceName string
param azureOpenAIName string
param azureOpenAIVideoName string

// For CosmosDB
param cosmosDBAccountName string

// For BLOB SAS TOKEN
param emailStorageAccountName string
param emailStorageContainerName string

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' existing = {
  name: keyVaultName
}

// https://learn.microsoft.com/en-us/azure/azure-resource-manager/bicep/scenarios-secrets#look-up-secrets-dynamically
resource openAIAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' existing = {
  name: azureOpenAIName
}

resource keyVaultSecret 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'OPENAI-API-KEY'
  properties: {
    value: openAIAccount.listKeys().key1
  }
}

resource keyVaultSecret2 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'OPENAI-MULTI-MODAL-API-KEY'
  properties: {
    value: openAIAccount.listKeys().key1
  }
}

resource openAIVisionAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' existing = {
  name: azureOpenAIVideoName
}

resource keyVaultSecret3 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'OPENAI-VISION-API-KEY'
  properties: {
    value: openAIVisionAccount.listKeys().key1
  }
}

resource documentIntelligenceAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' existing = {
  name: documentIntelligenceName
}

resource keyVaultSecret4 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'FORM-RECOGNIZER-API-KEY'
  properties: {
    value: documentIntelligenceAccount.listKeys().key1
  }
}

resource visionServicesAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' existing = {
  name: computerVisionName
}

resource keyVaultSecret5 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'COGNITIVE-SERVICE-KEY'
  properties: {
    value: visionServicesAccount.listKeys().key1
  }
}

resource databaseAccount 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' existing = {
  name: cosmosDBAccountName
}

resource keyVaultSecret6 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'COSMOSDB-API-KEY'
  properties: {
    value: databaseAccount.listKeys().primaryMasterKey
  }
}

// Generate a SAS token for the BLOB storage account
// Store the SAS token in Key Vault
// Based on https://ochzhen.com/blog/storage-account-sas-tokens-access-keys-connection-strings
resource storageAccount 'Microsoft.Storage/storageAccounts@2023-01-01' existing = {
  name: emailStorageAccountName
}

// Expiry: 1 year from today
var expiryDate = dateTimeAdd(baseTime, 'P1Y')

var sasConfig = {
  canonicalizedResource: '/blob/${storageAccount.name}/${emailStorageContainerName}' // Entire container  
  signedResource: 'c' // Container level
  signedPermission: 'r'
  signedExpiry: expiryDate
  signedProtocol: 'https'
  keyToSign: 'key1'
}

// Use sasConfig to generate a Service SAS token
var sasToken = '[\'${storageAccount.listServiceSas(storageAccount.apiVersion, sasConfig).serviceSasToken}\']'
var sasTokenBytes = base64(sasToken)

resource keyVaultSecret7 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'BLOB-STORE-SAS-TOKEN'
  properties: {
    value: sasTokenBytes
  }
}
