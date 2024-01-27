// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.samples.controller;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

import com.azure.spring.samples.ai.AzureAIOperation;
import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.anomaly.EmailAnomaly;
import com.azure.spring.samples.anomaly.attachment.AutoInsuranceClaimAnomaly;
import com.azure.spring.samples.anomaly.attachment.CommercialInsuranceApplicationAnomaly;
import com.azure.spring.samples.anomaly.attachment.DefaultAttachmentAnomaly;
import com.azure.spring.samples.anomaly.attachment.WorkersCompensationApplicationAnomaly;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.aoai.AzureOpenAIOperation.AOAIConnectionType;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentData;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.model.AttachmentSearchResult;
import com.azure.spring.samples.model.EmailData;
import com.azure.spring.samples.model.EmailSearchResult;
import com.azure.spring.samples.model.SearchItem;
import com.azure.spring.samples.utils.Category;

@RestController
public class EmailMessageReviewController {

    private static Logger logger = LoggerFactory.getLogger(EmailMessageReviewController.class);

    // Azure Blob Store access
    @Value("${azure.blob.store.sas.token}")
    private String blobStoreSASToken;

    // Azure CosmosDB access
    @Value("${azure.cosmos.uri}")
    private String azureCosmosURI;
    @Value("${azure.cosmos.key}")
    private String azureCosmosKey;
    @Value("${azure.cosmos.database}")
    private String azureCosmosDatabaseName;
    @Value("${azure.cosmos.container}")
    private String azureCosmosContainerName;
    
    // Azure Open AI access
    @Value("${azure.aoai.endpoint}")
    private String aoaiEndpoint;
    @Value("${azure.aoai.key}")
    private String aoaiKey;
    @Value("${azure.aoai.engine}")
    private String aoaiModel;
    @Value("${azure.aoai.version}")
    private String aoaiVersion;

    // GPT4 VISION API access
    @Value("${azure.aoai.vision.endpoint}")
    private String aoaiVisionEndpoint;
    @Value("${azure.aoai.vision.key}")
    private String aoaiVisionKey;
    @Value("${azure.aoai.vision.engine}")
    private String aoaiVisionModel;
    @Value("${azure.aoai.vision.version}")
    private String aoaiVisionVersion;
    
    // All common Azure Cognitive Services access
    @Value("${azure.cognitive.service.endpoint}")
    private String aiEndpoint;
    @Value("${azure.cognitive.service.key}")
    private String aiKey;
    @Value("${azure.aoai.vision.video.index}")
    private String aiVideoIndexName;
    @Value("${azure.ai.video.api.version}")
    private String aiVideoAPIVersion;
    
    public EmailMessageReviewController() {
    }
  
    /**
     * HTTP - let AOAI identify any email with potential errors
     */
    @RequestMapping(value = "/api/emailMessageReview/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getEmailMessageReviewSummary(@PathVariable String id) {
        logger.info("GET request access '/api/emailMessageReview' path with item: {}", id);

    	CosmosDBOperation cosmosDB = new CosmosDBOperation(
				azureCosmosURI, 
				azureCosmosKey,
				azureCosmosDatabaseName,
				azureCosmosContainerName
			);

        EmailAnomaly eAnomaly = new EmailAnomaly(cosmosDB);
        List<String> reviewRemarks = new ArrayList<>();
        String intentContentReviewRemarks = eAnomaly.checkIntentContentGap(id);
        reviewRemarks.add(intentContentReviewRemarks);
        String contentModerationReviewRemark = eAnomaly.checkContentModeration(id);
        reviewRemarks.add(contentModerationReviewRemark);
        
        logger.info("Got review remark as {}", reviewRemarks);
        return new ResponseEntity<>(reviewRemarks, HttpStatus.OK);
    }
    
    /**
     * HTTP - let AOAI identify any email with potential errors
     */
    @RequestMapping(value = "/api/attachmentReview/{id}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> getAttachmentReviewSummary(@PathVariable String id) {
        logger.info("GET request access '/api/attachmentReview' path with attachment id : {}", id);

        // Read the attachment category and call the AttachmentAnomaly.review method
    	CosmosDBOperation cosmosDB = new CosmosDBOperation(
				azureCosmosURI, 
				azureCosmosKey,
				azureCosmosDatabaseName,
				azureCosmosContainerName
			);
        String attachmentCategory = getAttachmentCategoryByAttachmentId(cosmosDB, id);
        if (attachmentCategory == null) {
            List<String> reviewSummary = new ArrayList<>();
        	String errorString = String.format("Could not find the category for the attachment by id %s", id);
        	reviewSummary.add(errorString);
        	logger.info(errorString);
            cosmosDB.close();
            return new ResponseEntity<>(reviewSummary, HttpStatus.OK);
        }
        AzureOpenAIOperation aoaiOps = new AzureOpenAIOperation(aoaiEndpoint, aoaiKey, aoaiModel, aoaiVersion, AOAIConnectionType.SDK);
        AttachmentAnomaly anomaly;
        if (StringUtils.compareIgnoreCase("workers-compensation-application", attachmentCategory) == 0) {
        	anomaly = new WorkersCompensationApplicationAnomaly(cosmosDB, aoaiOps);
        } else if (StringUtils.compareIgnoreCase("commercial-insurance-application", attachmentCategory) == 0) {
        	anomaly = new CommercialInsuranceApplicationAnomaly(cosmosDB, aoaiOps);
        } else if (StringUtils.compareIgnoreCase("auto-insurance-claim", attachmentCategory) == 0) {
        	anomaly = new  AutoInsuranceClaimAnomaly(cosmosDB, aoaiOps);
        } else {
        	// For the files with unknown classification, need to run the content
        	// by GPT4 regular if content is text, else by Vision API if image/video
            AzureOpenAIOperation aoaiVisionOps = new AzureOpenAIOperation (
            											aoaiVisionEndpoint, 
            											aoaiVisionKey, 
            											aoaiVisionModel, 
            											aoaiVisionVersion, 
            											AOAIConnectionType.HTTP
            										);
            AzureAIOperation aiOps = new AzureAIOperation(aiEndpoint, aiKey, aiVideoIndexName, aiVideoAPIVersion);
        	anomaly = new DefaultAttachmentAnomaly(cosmosDB, aoaiVisionOps, aiOps, blobStoreSASToken);
        }
        @SuppressWarnings("unchecked")
		List<String> reviewSummary = (List<String>) anomaly.getAttachmentAnomaly(id);

        cosmosDB.close();
        logger.info("Got review remark as {}", reviewSummary);
        return new ResponseEntity<>(reviewSummary, HttpStatus.OK);
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

    	CosmosDBOperation cosmosDB = new CosmosDBOperation(
    										azureCosmosURI, 
    										azureCosmosKey,
    										azureCosmosDatabaseName,
    										azureCosmosContainerName
    									);
    	String sqlStatement = "SELECT TOP 5 * FROM EmailExtracts e " +
    							"WHERE e.messageType IN ('email-body') " +
    							"AND e.sender IN ('" + 
    							searchQuery + "') " + 
    							"ORDER BY e.upsertTime DESC";    	
  		Iterator<EmailData> iterateED = cosmosDB.query(sqlStatement, EmailData.class);
  		
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
  	    	Iterator<AttachmentData> iterateAD = cosmosDB.query(sqlStatement, AttachmentData.class);
  	    	if (iterateAD != null) {
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
  	    	    	Iterator<AttachmentExtractsData> iterateAED = cosmosDB.query(sqlStatement, AttachmentExtractsData.class);
  	    	    	if (iterateAED != null) {
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
  		if (cosmosDB != null) {
  			cosmosDB.close();
  		}
  		return results;
    }
    
    public String getAttachmentCategoryByAttachmentId(CosmosDBOperation cosmosDB, String attachmentId) {
	    String sqlStatement = "SELECT * FROM EmailExtracts e " +
	    				"WHERE e.messageType IN ('email-attachment') " +
	    				"AND e.id IN ('" + attachmentId + "') ";
	    Iterator<AttachmentData> iterateAD = cosmosDB.query(sqlStatement, AttachmentData.class);
	    if (iterateAD != null && iterateAD.hasNext()) {
	    	AttachmentData ad = iterateAD.next();
	    	// Attachment has only one category
	    	return Category.getCategoryList(ad.getCategories()).get(0);
	    }
		return null;
    }
}
