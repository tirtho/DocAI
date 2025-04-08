@description('That name is the name of our application. It has to be unique.Type a name followed by your resource group name. (<name>-<resourceGroupName>)')
param aiServicesName string = 'docai-ai-non-prod'
param computerVisionServicesName string = 'docai-ai-vision-non-prod'
param azureOpenAIName string = 'docai-aoai'
param azureOpenAIVsionName string = 'docai-aoai-vision'
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

resource visionServicesAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' = {
  name: computerVisionServicesName
  location: location
  sku: {
    name: 'S1'
  }
  kind: 'ComputerVision'
  properties: {
    customSubDomainName: computerVisionServicesName
    publicNetworkAccess: 'Enabled'
    networkAcls: {
      defaultAction: 'Allow'
    }
    disableLocalAuth: false
  }
}

resource openAIAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' = {
  name: azureOpenAIName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  sku: {
    name: sku
  }
  kind: 'OpenAI'
  properties: {
    customSubDomainName: azureOpenAIName
    publicNetworkAccess: 'Enabled'
    networkAcls: {
      defaultAction: 'Allow'
    }
    disableLocalAuth: false
  }
}

resource gpt_4o_deployment 'Microsoft.CognitiveServices/accounts/deployments@2024-06-01-preview' = if (deployDeployments) {
  parent: openAIAccount
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

resource gpt_4_vision_deployment 'Microsoft.CognitiveServices/accounts/deployments@2024-06-01-preview' = if (deployDeployments) {
  parent: openAIAccount
  name: 'gpt-4-vision'
  sku: {
    name: 'Standard'
    capacity: 10
  }
  properties: {
    model: {
      format: 'OpenAI'
      name: 'gpt-4'
      version: 'vision-preview'
    }
    versionUpgradeOption: 'OnceNewDefaultVersionAvailable'
    currentCapacity: 10
  }
  dependsOn: [
    // Need to deploy gpt_4o_deployment first and WAIT, otherwise we'll get a conflict
    gpt_4o_deployment
  ]
}

resource openAIVisionAccount 'Microsoft.CognitiveServices/accounts@2023-05-01' = {
  name: azureOpenAIVsionName
  location: location
  sku: {
    name: sku
  }
  kind: 'OpenAI'
  properties: {
    customSubDomainName: azureOpenAIVsionName
    publicNetworkAccess: 'Enabled'
    networkAcls: {
      defaultAction: 'Allow'
    }
    disableLocalAuth: false
  }
}

resource gpt_4_vision_deployment_forVisionOpenAI 'Microsoft.CognitiveServices/accounts/deployments@2024-06-01-preview' = if (deployDeployments) {
  parent: openAIVisionAccount
  name: 'gpt-4-vision'
  sku: {
    name: 'Standard'
    capacity: 10
  }
  properties: {
    model: {
      format: 'OpenAI'
      name: 'gpt-4'
      version: 'vision-preview'
    }
    versionUpgradeOption: 'OnceNewDefaultVersionAvailable'
    currentCapacity: 10
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

output openAIPrincipalId string = openAIAccount.identity.principalId
output documentIntelligencePrincipalId string = documentIntelligenceAccount.identity.principalId
output azureOpenAIName string = openAIAccount.name
output azureOpenAIVsionName string = openAIVisionAccount.name
output documentIntelligenceName string = documentIntelligenceAccount.name
output aiServicesName string = aiServicesAccount.name
output computerVisionServicesName string = visionServicesAccount.name
output azureOpenAIEndpoint string = openAIAccount.properties.endpoint
output azureOpenAIVsionEndpoint string = openAIVisionAccount.properties.endpoint
output documentIntelligenceEndpoint string = documentIntelligenceAccount.properties.endpoint
output aiServicesEndpoint string = aiServicesAccount.properties.endpoint
output computerVisionServicesEndpoint string = visionServicesAccount.properties.endpoint
