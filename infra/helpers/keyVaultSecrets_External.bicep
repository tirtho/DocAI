param keyVaultName string
@secure()
param webAppClientSecret string
@secure()
param graphAPIClientSecret string


resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' existing = {
  name: keyVaultName
}

resource keyVaultSecret1 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'DOCAI-APP-CLIENT-SECRET'
  properties: {
    value: webAppClientSecret
  }
}

resource keyVaultSecret2 'Microsoft.KeyVault/vaults/secrets@2019-09-01' = {
  parent: keyVault
  name: 'GRAPH-API-CLIENT-SECRET'
  properties: {
    value: graphAPIClientSecret
  }
}
