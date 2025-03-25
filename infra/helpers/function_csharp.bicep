param location string = resourceGroup().location
param csfunctionAppName string
param vnetSubnetId string
param storageAccountName string
param hostingPlanID string
param applicationInsightsKey string

@description('Only required for Linux app to represent runtime stack in the format of \'runtime|runtimeVersion\'. For example: \'python|3.9\'')
param cslinuxFxVersion string = 'DOTNET-ISOLATED|8.0'

param functionPlanOS string = 'Linux'
var isReserved = ((functionPlanOS == 'Linux') ? true : false)

resource storageAccount 'Microsoft.Storage/storageAccounts@2022-05-01' existing = {
  name: storageAccountName
}

resource csFunctionApp 'Microsoft.Web/sites@2022-03-01' = {
  name: csfunctionAppName
  location: location
  kind: (isReserved ? 'functionapp,linux' : 'functionapp')
  properties: {
    reserved: isReserved
    serverFarmId: hostingPlanID
    siteConfig: {
      linuxFxVersion: (isReserved ? cslinuxFxVersion : null)
      appSettings: [
        {
          name: 'APPINSIGHTS_INSTRUMENTATIONKEY'
          value: applicationInsightsKey
        }
        {
          name: 'AzureWebJobsStorage'
          value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storageAccount.listKeys().keys[0].value}'
        }
        {
          name: 'WEBSITE_CONTENTAZUREFILECONNECTIONSTRING'
          value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storageAccount.listKeys().keys[0].value};'
        }
        {
          name: 'WEBSITE_CONTENTSHARE'
          value: toLower(csfunctionAppName)
        }
        {
          name: 'FUNCTIONS_EXTENSION_VERSION'
          value: '~4'
        }
        {
          name: 'FUNCTIONS_WORKER_RUNTIME'
          value: 'dotnet-isolated'
        }
      ]
    }
  }
}

resource functionAppName_virtualNetwork_cs 'Microsoft.Web/sites/networkConfig@2022-03-01' = {
  parent: csFunctionApp
  name: 'virtualNetwork'
  properties: {
    subnetResourceId: vnetSubnetId
    swiftSupported: true
  }
}

output cSharpFunctionName string = csFunctionApp.name
