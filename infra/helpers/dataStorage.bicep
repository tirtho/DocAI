param emailStorageAccountName string
param docIntelStorageAccountName string

var emailContainerName = 'docai'
var docIntelContainerName = 'doc-intel-models'

// Storage for Email
module emailStorageAccount '../core/storage/storage-account.bicep' = {
  name: emailStorageAccountName
  params: {
    name: emailStorageAccountName
    containers: [{ name: emailContainerName, publicAccess: 'Blob' }]
  }
}

// Storage for Document Intelligence
module docIntelStorageAccount '../core/storage/storage-account.bicep' = {
  name: docIntelStorageAccountName
  params: {
    name: docIntelStorageAccountName
    containers: [{ name: docIntelContainerName }]
  }
}

output emailStorageAccountId string = emailStorageAccount.outputs.id
output emailStorageAccountName string = emailStorageAccount.outputs.name
output emailContainerName string = emailContainerName
output docIntelStorageAccountName string = docIntelStorageAccount.outputs.name
output docIntelContainerName string = docIntelContainerName
