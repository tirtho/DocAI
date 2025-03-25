@description('The name of the virtual network to be created.')
param vnetName string = 'vnet-docai'

@description('The name of the subnet to be created within the virtual network.')
param subnetName string = 'subnet-docai'

param location string = resourceGroup().location
var vnetAddressPrefix = '10.0.0.0/16'
var subnetAddressPrefix = '10.0.0.0/24'

var serviceEndpointsAll = [
  {
    service: 'Microsoft.Storage'
  }
  {
    service: 'Microsoft.KeyVault'
  }
]

resource vnet 'Microsoft.Network/virtualNetworks@2022-05-01' = {
  name: vnetName
  location: location
  properties: {
    addressSpace: {
      addressPrefixes: [
        vnetAddressPrefix
      ]
    }
    subnets: [
      {
        name: subnetName
        properties: {
          addressPrefix: subnetAddressPrefix
          delegations: [
            {
              name: 'delegation'
              properties: {
                serviceName: 'Microsoft.Web/serverFarms'
              }
            }
          ]
          serviceEndpoints: serviceEndpointsAll
        }
      }
    ]
  }
}

output subnetId string = resourceId('Microsoft.Network/virtualNetworks/subnets', vnetName, subnetName)
