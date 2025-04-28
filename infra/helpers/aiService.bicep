@description('That name is the name of our application. It has to be unique.Type a name followed by your resource group name. (<name>-<resourceGroupName>)')
param aiServicesName string = 'docai-ai-non-prod'
param documentIntelligenceName string = 'docai-doc-intel-non-prod'
param location string = resourceGroup().location
param deployDeployments bool = true

// This is needed because Document Intelligence API Public Preview (2024-07-31-preview) is not available in all regions
// If we do not deploy to a region where it is available, we are unable to connect with the supported Python Libraries
// which are used for the deployment of the models
// https://learn.microsoft.com/en-us/azure/ai-services/document-intelligence/versioning/sdk-overview-v4-0?view=doc-intel-4.0.0&tabs=python
// Supported Locations: East Us, West US2, West Europe, North Central US
@description('Location of the Document Intelligence Account. Default is the resource group location.')
param documentIntelligenceLocationOverride string = resourceGroup().location

// Deploying the deployments for OpenAI
// https://gist.githubusercontent.com/tsjdev-apps/5c5949fb94dc59ff3a6f3fa495c37d69/raw/2d3a20632240bcfbe538c403fcfe2fc0841da278/openai.resources.bicep

@allowed([
  'S0'
])
param sku string = 'S0'

resource aiServicesAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' = {
  name: aiServicesName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  sku: {
    name: sku
  }
  kind: 'AIServices'
  properties: {
    customSubDomainName: aiServicesName
    publicNetworkAccess: 'Enabled'
    networkAcls: {
      defaultAction: 'Allow'
    }
    disableLocalAuth: false
  }
}

resource gpt_4o_deployment 'Microsoft.CognitiveServices/accounts/deployments@2024-06-01-preview' = if (deployDeployments) {
  parent: aiServicesAccount
  name: 'gpt-4o'
  sku: {
    name: 'GlobalStandard'
    capacity: 100
  }
  properties: {
    model: {
      format: 'OpenAI'
      name: 'gpt-4o'
      version: '2024-08-06'
    }
    versionUpgradeOption: 'OnceCurrentVersionExpired'
    currentCapacity: 100
    raiPolicyName: 'Microsoft.DefaultV2'
  }
}

resource documentIntelligenceAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' = {
  name: documentIntelligenceName
  location: documentIntelligenceLocationOverride
  identity: {
    type: 'SystemAssigned'
  }
  sku: {
    name: sku
  }
  kind: 'FormRecognizer'
  properties: {
    customSubDomainName: documentIntelligenceName
    publicNetworkAccess: 'Enabled'
    networkAcls: {
      defaultAction: 'Allow'
    }
    disableLocalAuth: false
  }
}

output openAIPrincipalId string = aiServicesAccount.identity.principalId
output documentIntelligencePrincipalId string = documentIntelligenceAccount.identity.principalId
output documentIntelligenceName string = documentIntelligenceAccount.name
output aiServicesName string = aiServicesAccount.name
output contentUnderstandingName string = aiServicesAccount.name
output contentUnderstandingEndpoint string = 'https://${aiServicesAccount.name}.services.ai.azure.com/'
output azureOpenAIName string = aiServicesAccount.name
output azureOpenAIEndpoint string = 'https://${aiServicesAccount.name}.openai.azure.com/'
output documentIntelligenceEndpoint string = documentIntelligenceAccount.properties.endpoint

