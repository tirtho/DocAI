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
import com.azure.spring.samples.model.LinkedInCompanyData;
import com.azure.spring.samples.model.SearchItem;
import com.azure.spring.samples.model.SearchResult;

@RestController
public class LinkedInScoreController {

    private static Logger logger = LoggerFactory.getLogger(LinkedInScoreController.class);
   
    @Value("${azure.cosmos.uri}")
    private String azureCosmosURI;
    @Value("${azure.cosmos.key}")
    private String azureCosmosKey;
    @Value("${azure.cosmos.database}")
    private String azureCosmosDatabaseName;
    @Value("${azure.cosmos.container}")
    private String azureCosmosContainerName;
    
    public LinkedInScoreController() {
    }
  
    /**
     * HTTP GET SCORE
     */
    @RequestMapping(value = "/api/linkedInScore/{keyPhrases}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getInsurnaceScore(@PathVariable String keyPhrases) {
        logger.info("GET request access '/api/linkedInScore' path with item: {}", keyPhrases);
        String score = Double.toString(Math.random());

        return new ResponseEntity<>(score, HttpStatus.OK);
    }
    
    /**
     * HTTP POST NEW ONE
     */
    @RequestMapping(value = "/api/linkedInScore", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> linkedInForInsurability(@RequestBody SearchItem searchItem) {
        logger.info("POST request access '/api/linkedInScore' path with item: {}", searchItem);
        
        try {
        	List<SearchResult> searchResults = searchLinkedIn(searchItem.getDescription());

        	if (searchResults != null && !searchResults.isEmpty()) {
        		return new ResponseEntity<>(searchResults, HttpStatus.OK);
        	} else {
            	logger.info("Found Nothing from searching for Company {} in LinkedIn", searchItem.getDescription());
                return new ResponseEntity<>("Nothing found", HttpStatus.NOT_FOUND);            
        	}
        } catch (Exception e) {
        	logger.info("LinkedIn Search failed: {}", e);
        	return new ResponseEntity<>("Failed Search", HttpStatus.NOT_FOUND);
        }
    }
    
    public List<SearchResult> searchLinkedIn (String searchQuery) throws Exception {
    	// Connect to CosmosDB Sql API and search for the Company data
    	CosmosClient cosmosClient = new CosmosClientBuilder()
    									.endpoint(azureCosmosURI)
    									.key(azureCosmosKey)
    									.buildClient();
    	String sqlStatement = "SELECT * FROM CompanyInfo c WHERE c.company = '" + searchQuery + "'";
    	
    	CosmosContainer container = cosmosClient.getDatabase(azureCosmosDatabaseName)
										.getContainer(azureCosmosContainerName);
    	CosmosPagedIterable<LinkedInCompanyData> companies = container
    															.queryItems(
    																sqlStatement, 
    																new CosmosQueryRequestOptions(), 
    																LinkedInCompanyData.class);
  		List<SearchResult> results = new ArrayList<SearchResult>();
  		Iterator<LinkedInCompanyData> iterateCompanies = companies.iterator();
  		while (iterateCompanies.hasNext()) {
  			LinkedInCompanyData company = iterateCompanies.next();
  			logger.info("Company Info found : {}", company.toString());

  	    	SearchResult sr = new SearchResult(
  	    			SearchResult.SEARCH_TYPE_LINKEDIN, 
  	    			company.getCompany(), 
  	    			company.getId(), 
  	    			company.getLinkedInURL(), 
  	    			company.getLinkedInCompanyID(), 
  	    			null,
  	    			null);
  	    	results.add(sr);
  		}
  		if (cosmosClient != null) {
  			cosmosClient.close();
  		}
  		return results;

    }
    
}
