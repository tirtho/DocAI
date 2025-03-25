// https://blog.johnfolberth.com/assigning-cosmos-data-plane-roles-via-rbac-w-bicep/

@description('CosmosDB Account to apply the role assignment to')
param databaseAccountName string

@description('Principal id to assign the role to')
param principalId string

@description('Role definition id to assign to the principal')
@allowed([
  '00000000-0000-0000-0000-000000000001' // Built-in role 'Azure Cosmos DB Built-in Data Reader'
  '00000000-0000-0000-0000-000000000002' // Built-in role 'Azure Cosmos DB Built-in Data Contributor'
])
param roleDefinitionId string = '00000000-0000-0000-0000-000000000002'

resource databaseAccount 'Microsoft.DocumentDB/databaseAccounts@2023-04-15' existing = {
  name: databaseAccountName
}

resource roleDefinition 'Microsoft.DocumentDB/databaseAccounts/sqlRoleDefinitions@2024-05-15' = {
  parent: databaseAccount
  name: '00000000-0000-0000-0000-000000000002'
  properties: {
    roleName: 'Cosmos DB Built-in Data Contributor'
    type: 'BuiltInRole'
    assignableScopes: [
      databaseAccount.id
    ]
    permissions: [
      {
        dataActions: [
          'Microsoft.DocumentDB/databaseAccounts/readMetadata'
          'Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/*'
          'Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers/items/*'
        ]
        notDataActions: []
      }
    ]
  }
}

var roleAssignmentId = guid(roleDefinitionId, principalId, databaseAccount.id)

resource sqlRoleAssignment 'Microsoft.DocumentDB/databaseAccounts/sqlRoleAssignments@2023-04-15' = {
  name: roleAssignmentId
  parent: databaseAccount
  properties: {
    principalId: principalId
    roleDefinitionId: roleDefinition.id
    scope: databaseAccount.id
  }
}
