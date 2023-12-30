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
import com.azure.spring.samples.model.AttachmentData;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.model.AttachmentSearchResult;
import com.azure.spring.samples.model.EmailData;
import com.azure.spring.samples.model.EmailSearchResult;
import com.azure.spring.samples.model.SearchItem;

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
    @RequestMapping(value = "/api/emailMessageReview/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getEmailMessageReviewSummary(@PathVariable String id) {
        logger.info("GET request access '/api/emailMessageReview' path with item: {}", id);
        String score = Double.toString(Math.random());
		// TODO: For known forms, use AOAI to validate, flag and fix fields
        return new ResponseEntity<>(id, HttpStatus.OK);
    }
    
    /**
     * HTTP POST NEW ONE
     */
    @RequestMapping(value = "/api/emailMessageReview", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getEmailMessageDetails(@RequestBody SearchItem searchItem) {
        logger.info("POST request access '/api/emailMessageReview' path with item: {}", searchItem);
        
        try {
        	List<EmailSearchResult> searchResults = searchEmailDetailsBySenderEmailAddress(searchItem.getDescription());

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
    
    public List<EmailSearchResult> searchEmailDetailsBySenderEmailAddress (String searchQuery) throws Exception {
    	CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
    	queryOptions.setQueryMetricsEnabled(true);

    	// Connect to CosmosDB Sql API and search for the Company data
    	CosmosClient cosmosClient = new CosmosClientBuilder()
    									.endpoint(azureCosmosURI)
    									.key(azureCosmosKey)
    									.buildClient();
    	String sqlStatement = "SELECT TOP 5 * FROM EmailExtracts e " +
    							"WHERE e.messageType IN ('email-body') " +
    							"AND e.sender IN ('" + 
    							searchQuery + "') " + 
    							"ORDER BY e.upsertTime DESC";
    	
    	CosmosContainer container = cosmosClient.getDatabase(azureCosmosDatabaseName)
										.getContainer(azureCosmosContainerName);
    	CosmosPagedIterable<EmailData> emailExtractsIterable = container.queryItems(
		    																sqlStatement, 
		    																queryOptions, 
		    																EmailData.class
		    															);
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
    	
  		Iterator<EmailData> iterateED = emailExtractsIterable.iterator();
    	List<EmailSearchResult> results = new ArrayList<>();
    	// For each Email Message
  		while (iterateED.hasNext()) {
  			EmailData ed = iterateED.next();
  			logger.info("EmailExtract Info found : {}", ed.toString());
  	    	EmailSearchResult esr = new EmailSearchResult();
  	    	esr.setId(ed.getId()); esr.setBodyPreview(ed.getBodyPreview());
  	    	esr.setCategories(ed.getCategories().replace("\\\"", "\'")); esr.setReceivedTime(ed.getReceivedTime());
  	    	esr.setIsHTML(ed.getIsHTML()); esr.setHasAttachment(ed.getHasAttachment());
  	    	esr.setMessageId(ed.getMessageId());esr.setMessageType(ed.getMessageType());
  	    	esr.setSender(ed.getSender());esr.setSubject(ed.getSubject());esr.setUpsertTime(ed.getUpsertTime());esr.setUrl(ed.getUrl()); 	
  	    	// Get the attachment list for the given email message Id
  	    	sqlStatement = "SELECT * FROM EmailExtracts e " +
					"WHERE e.messageType IN ('email-attachment') " +
					"AND e.messageId IN ('" + ed.getMessageId() + "') " + 
					"ORDER BY e.upsertTime DESC";
  	    	CosmosPagedIterable<AttachmentData> attachmentDataIterable = container.queryItems(
					sqlStatement, 
					queryOptions, 
					AttachmentData.class
				);
  	    	if (attachmentDataIterable != null) {
  	    		Iterator<AttachmentData> iterateAD = attachmentDataIterable.iterator();
  	    		List<AttachmentSearchResult> asrList = new ArrayList<>();
  	    		// For each attachment
  	    		while (iterateAD.hasNext()) {
  	    			AttachmentData ad = iterateAD.next();
  	    			logger.info("AttachmentData Info found : {}", ad.toString());
  	    			AttachmentSearchResult asr = new AttachmentSearchResult();
  	    			asr.setAttachmentName(ad.getAttachmentName()); asr.setUrl(ad.getUrl());
  	    			asr.setCategories(ad.getCategories());
  	    			asr.setMessageType(ad.getMessageType());
  	    			asr.setModelType(ad.getModelType());
  	    			asr.setId(ad.getId());
  	    			
  	    			// Now get the extracted data for the attachment
  	    	    	sqlStatement = "SELECT * FROM EmailExtracts e " +
  	  					"WHERE e.messageType IN ('email-attachment-extracts') " +
  	  					"AND e.messageId IN ('" + ed.getMessageId() + "') " + 
  	  					"AND e.attachmentName IN ('" + ad.getAttachmentName() + "') " +
  	  					"ORDER BY e.upsertTime DESC";
  	    	    	CosmosPagedIterable<AttachmentExtractsData> attachmentExtractsDataIterable = container.queryItems(
  	  					sqlStatement, 
  	  					queryOptions, 
  	  					AttachmentExtractsData.class
  	  				);
  	    	    	if (attachmentExtractsDataIterable != null) {
  	    	    		Iterator<AttachmentExtractsData> iterateAED = attachmentExtractsDataIterable.iterator();
  	    	    		// An attachment has only one AttachmentExtractsData
  	    	    		// So, just getting the first one from the list below
  	    	    		if (iterateAED.hasNext()) {
  	    	    			AttachmentExtractsData aed = iterateAED.next();
  	    	    			logger.info("AttachmentExtractsData Info for attachment:{} found as:{}", 
  	    	    					aed.getAttachmentName(), aed.toString());  	    	    			
  	    	    			asr.setIsHandwritten(aed.getIsHandwritten());
  	    	    			asr.setModelId(aed.getModelId());
  	    	    			asr.setFrAPIVersion(aed.getFrAPIVersion());
  	    	    			asr.setExtracts(aed.getExtracts());
  	    	    		}
  	    	    	}
  	    	    	// Add the attachment data and extracts to the list
  	    			asrList.add(asr);
  	    		}
  	    		esr.setAttachments(asrList);
  	    	} else {
  	  	    	logger.info("No AttachmentData Info found");  	    		
  	    	}
  	    	results.add(esr);
  		}
  		if (cosmosClient != null) {
  			cosmosClient.close();
  		}
  		return results;

    }
    
}
