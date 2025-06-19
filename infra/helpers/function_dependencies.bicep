param location string = resourceGroup().location
param resourceToken string

@description('Name of the Log Analytics workspace to use for the Logic App.')
param logAnalyticsName string

@description('Specifies the OS used for the Azure Function hosting plan.')
@allowed([
  'Windows'
  'Linux'
])
param functionPlanOS string = 'Linux'

var isReserved = ((functionPlanOS == 'Linux') ? true : false)
var hostingPlanName = 'asp-func'
var applicationInsightsName = 'ai-func'
var storageAccountName = 'safunc${resourceToken}'

param functionAppPlanSku string = 'EP1'

resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2020-08-01' existing = {
  name: logAnalyticsName
}

// Storage for Azure Functions (CS + PY)
module storageAccount '../core/storage/storage-account.bicep' = {
  name: storageAccountName
  params: {
    // name: !empty(storageAccountName) ? storageAccountName : '${abbrs.storageStorageAccounts}${resourceToken}${environmentName}'
    name: storageAccountName
    location: location
  }
}

// App Service Plan for Azure Functions (CS + PY)
resource hostingPlan 'Microsoft.Web/serverfarms@2022-03-01' = {
  name: hostingPlanName
  location: location
  sku: {
    tier: 'ElasticPremium'
    name: functionAppPlanSku
    family: 'EP'
  }
  properties: {
    maximumElasticWorkerCount: 20
    reserved: isReserved
  }
  kind: 'elastic'
}

resource applicationInsights 'Microsoft.Insights/components@2020-02-02' = {
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

output storageAccountName string = storageAccount.name
output hostingPlanID string = hostingPlan.id
output applicationInsightsKey string = applicationInsights.properties.InstrumentationKey
