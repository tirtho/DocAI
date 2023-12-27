// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.samples.controller;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.azure.spring.samples.model.Category;
import com.azure.spring.samples.model.EmailExtractData;
import com.azure.spring.samples.model.SearchItem;
import com.azure.spring.samples.model.SearchResult;

@RestController
public class EmailMessageReviewController {

    private static Logger logger = LoggerFactory.getLogger(EmailMessageReviewController.class);
   
    @Value("${azure.cosmos.uri}")
    private String azureCosmosURI;
    @Value("${azure.cosmos.key}")
    private String azureCosmosKey;
    @Value("${azure.cosmos.database}")
    private String azureCosmosDatabaseName;
    @Value("${azure.cosmos.container}")
    private String azureCosmosContainerName;
    
    public EmailMessageReviewController() {
    }
  
    /**
     * HTTP - let AOAI identify any field with potential errors
     */
    @RequestMapping(value = "/api/emailMessageReview/{messageId}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getFormFieldsFixedByAI(@PathVariable String messageId) {
        logger.info("GET request access '/api/emailMessageReview' path with item: {}", messageId);
        String score = Double.toString(Math.random());
		// TODO: For known forms, use AOAI to validate, flag and fix fields
        return new ResponseEntity<>(score, HttpStatus.OK);
    }
    
    /**
     * HTTP POST NEW ONE
     */
    @RequestMapping(value = "/api/emailMessageReview", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEmailMessageDetails(@RequestBody SearchItem searchItem) {
        logger.info("POST request access '/api/emailMessageReview' path with item: {}", searchItem);
        
        try {
        	List<SearchResult> searchResults = searchEmailDetailsBySenderEmailAddress(searchItem.getDescription());

        	if (searchResults != null && !searchResults.isEmpty()) {
        		return new ResponseEntity<>(searchResults, HttpStatus.OK);
        	} else {
            	logger.info("Did not find any message by search string:{} in CosmosDB", searchItem.getDescription());
                return new ResponseEntity<>("Nothing found", HttpStatus.NOT_FOUND);            
        	}
        } catch (Exception e) {
        	logger.info("Finding email messages for search item {} raised exception:{}", searchItem, e);
        	return new ResponseEntity<>("Search Failed", HttpStatus.NOT_FOUND);
        }
    }
    
    public List<SearchResult> searchEmailDetailsBySenderEmailAddress (String searchQuery) throws Exception {
    	CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
    	queryOptions.setQueryMetricsEnabled(true);

    	// Connect to CosmosDB Sql API and search for the Company data
    	CosmosClient cosmosClient = new CosmosClientBuilder()
    									.endpoint(azureCosmosURI)
    									.key(azureCosmosKey)
    									.buildClient();
    	String sqlStatement = "SELECT TOP 5 * FROM EmailExtracts e " +
    							"WHERE e.messageType IN ('email-body') " +
    							"AND e.sender = 'Tirthankar.Barari@microsoft.com' " + 
    							"ORDER BY e.upsertTime DESC";
    	
    	CosmosContainer container = cosmosClient.getDatabase(azureCosmosDatabaseName)
										.getContainer(azureCosmosContainerName);
    	CosmosPagedIterable<EmailExtractData> emailExtractsIterable = container
		    															.queryItems(
		    																sqlStatement, 
		    																queryOptions, 
		    																EmailExtractData.class);
//    	emailExtractsIterable.iterableByPage(10).forEach(cosmosItemPropertiesFeedResponse -> {
//    		logger.info("Got a page of query result with {} items and request charge of {}",
//    	            cosmosItemPropertiesFeedResponse.getResults().size(), 
//    	            cosmosItemPropertiesFeedResponse.getRequestCharge());
//    	    logger.info("Item Ids {}", cosmosItemPropertiesFeedResponse
//    	            .getResults()
//    	            .stream()
//    	            .map(EmailExtractData::getId)
//    	            .collect(Collectors.toList()));
//    	    });
    	
  		Iterator<EmailExtractData> iterateEmailExtracts = emailExtractsIterable.iterator();
    	List<SearchResult> results = new ArrayList<SearchResult>();
  		while (iterateEmailExtracts.hasNext()) {
  			EmailExtractData emailExtractData = iterateEmailExtracts.next();
  			logger.info("EmailExtract Info found : {}", emailExtractData.toString());
  			List<String> theCategories = new ArrayList<String>();
  			for(Category category:emailExtractData.getCategories()) {
  	  			StringBuffer theCategoryString = new StringBuffer(); 
  				if(category.getCategory()!=null)
  					theCategoryString.append(category.getCategory());
  				if(category.getConfidence()!=null)
  					theCategoryString.append(":").append(category.getConfidence());
  				if(category.getPages()!= null)
  					theCategoryString.append(":").append(category.getPages());
  				theCategories.add(theCategoryString.toString());
  			} 			
  	    	SearchResult sr = new SearchResult(
  	    			SearchResult.SEARCH_TYPE_EMAIL_EXTRACT, 
  	    			emailExtractData.getBodyPreview(), 
  	    			emailExtractData.getId(), 
  	    			emailExtractData.getUrl(), 
  	    			emailExtractData.getMessageId(), 
  	    			theCategories,
  	    			null);
  	    	results.add(sr);
  		}
  		if (cosmosClient != null) {
  			cosmosClient.close();
  		}
  		return results;

    }
    
}
