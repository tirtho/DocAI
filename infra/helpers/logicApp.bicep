param logicAppName string

@description('Location for all resources.')
param location string = resourceGroup().location

param resourceToken string

@description('Name of the Log Analytics workspace to use for the Logic App.')
param logAnalyticsName string

var hostingPlanName = 'asp-logicApp'
var applicationInsightsName = 'ai-logic'
var storageAccountName = 'salogic${resourceToken}'

var contentShare = 'la-docai-referenceb9bb'

resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2020-08-01' existing = {
  name: logAnalyticsName
}

resource name_resource 'Microsoft.Web/sites@2022-03-01' = {
  name: logicAppName
  kind: 'functionapp,workflowapp'
  location: location
  tags: {
    'hidden-link:${resourceId('Microsoft.Web/sites', applicationInsightsName)}': 'Resource'
  }
  properties: {
    siteConfig: {
      appSettings: [
        {
          name: 'FUNCTIONS_EXTENSION_VERSION'
          value: '~4'
        }
        {
          name: 'FUNCTIONS_WORKER_RUNTIME'
          value: 'dotnet'
        }
        {
          name: 'WEBSITE_NODE_DEFAULT_VERSION'
          value: '~18'
        }
        {
          name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
          value: reference('microsoft.insights/components/${applicationInsightsName}', '2015-05-01').ConnectionString
        }
        {
          name: 'AzureWebJobsStorage'
          value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccountName};AccountKey=${listKeys(storageAccount.id,'2019-06-01').keys[0].value};EndpointSuffix=core.windows.net'
        }
        {
          name: 'WEBSITE_CONTENTAZUREFILECONNECTIONSTRING'
          value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccountName};AccountKey=${listKeys(storageAccount.id,'2019-06-01').keys[0].value};EndpointSuffix=core.windows.net'
        }
        {
          name: 'WEBSITE_CONTENTSHARE'
          value: contentShare
        }
        {
          name: 'AzureFunctionsJobHost__extensionBundle__id'
          value: 'Microsoft.Azure.Functions.ExtensionBundle.Workflows'
        }
        {
          name: 'AzureFunctionsJobHost__extensionBundle__version'
          value: '[1.*, 2.0.0)'
        }
        {
          name: 'APP_KIND'
          value: 'workflowApp'
        }
      ]
      cors: {}
      netFrameworkVersion: 'v6.0'
    }
    clientAffinityEnabled: false
    virtualNetworkSubnetId: null
    publicNetworkAccess: 'Enabled'
    httpsOnly: true
    serverFarmId: hostingPlan.id
  }
  identity: {
    type: 'SystemAssigned'
  }
  dependsOn: [
    ai_logic
  ]
}

// resource name_scm 'Microsoft.Web/sites/basicPublishingCredentialsPolicies@2022-09-01' = {
//   parent: name_resource
//   name: 'scm'
//   properties: {
//     allow: false
//   }
// }

// resource name_ftp 'Microsoft.Web/sites/basicPublishingCredentialsPolicies@2022-09-01' = {
//   parent: name_resource
//   name: 'ftp'
//   properties: {
//     allow: false
//   }
// }

resource hostingPlan 'Microsoft.Web/serverfarms@2023-12-01' = {
  name: hostingPlanName
  location: location
  kind: ''
  tags: {}
  properties: {
    targetWorkerCount: 1
    elasticScaleEnabled: true
    maximumElasticWorkerCount: 20
    zoneRedundant: false
  }
  sku: {
    tier: 'WorkflowStandard'
    name: 'WS1'
  }
  dependsOn: []
}

resource ai_logic 'microsoft.insights/components@2020-02-02' = {
  name: applicationInsightsName
  location: location
  tags: {
    'hidden-link:${resourceId('Microsoft.Web/sites', applicationInsightsName)}': 'Resource'
  }
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: logAnalyticsWorkspace.id
  }
  kind: 'web'
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: storageAccountName
  location: location
  sku: {
    name: 'Standard_LRS'
  }
  kind: 'StorageV2'
  properties: {
    supportsHttpsTrafficOnly: true
    minimumTlsVersion: 'TLS1_2'
    defaultToOAuthAuthentication: true
    allowBlobPublicAccess: false
  }
  dependsOn: []
}

output logicAppName string = name_resource.name
output logicAppPrincipalId string = name_resource.identity.principalId
