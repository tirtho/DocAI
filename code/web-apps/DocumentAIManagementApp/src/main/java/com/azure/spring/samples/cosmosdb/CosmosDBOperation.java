package com.azure.spring.samples.cosmosdb;

import java.util.Iterator;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.spring.samples.model.AttachmentExtractsData;

public class CosmosDBOperation {

    private CosmosQueryRequestOptions queryOptions;
    private CosmosClient cosmosClient;
    private CosmosContainer container;

    public CosmosDBOperation(String endpoint, String key, String databaseName, String containerName) {
		super();
    	queryOptions = new CosmosQueryRequestOptions();
    	queryOptions.setQueryMetricsEnabled(true);
    	
    	// Connect to CosmosDB Sql API and search for data
		cosmosClient = new CosmosClientBuilder().endpoint(endpoint).key(key).buildClient();
		container = cosmosClient.getDatabase(databaseName).getContainer(containerName);
    }
        
    public <T> Iterator<T> query(String sqlStatement, Class<T> responseClass) {
		CosmosPagedIterable<T> result = container.queryItems(sqlStatement, queryOptions, responseClass);
		if (result != null) {
			return result.iterator();
		}
		return null;
    }
    
    public CosmosItemResponse<AttachmentExtractsData> upsertAttachmentExtractsData(AttachmentExtractsData aed) {
    	return container.upsertItem(aed);
    }
    
    public void close() {
    	if (cosmosClient != null) {
    		cosmosClient.close();
    	}
    }
    
}
