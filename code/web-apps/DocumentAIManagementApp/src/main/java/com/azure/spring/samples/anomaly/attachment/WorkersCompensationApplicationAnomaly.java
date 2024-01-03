package com.azure.spring.samples.anomaly.attachment;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentData;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.google.gson.Gson;

public class WorkersCompensationApplicationAnomaly implements AttachmentAnomaly {

    public static Logger logger = LoggerFactory.getLogger(WorkersCompensationApplicationAnomaly.class);
    
	public static String FIND_MISSING_FIELDS_PROMPT = "The ApplicantName, "
														+ "ApplicantOfficePhone, "
														+ "ApplicantMobilePhone, "
														+ "ApplicantMailingAddress, "
														+ "and ApplicantEmailAddress "
														+ "are required fields in the json document below.  \r\n"
														+ "Find which fields have no value. Answer in the format as "
														+ "'All required fields are present' or 'Required field(s) <field names> are absent' "
														+ "\n ";
    private CosmosDBOperation cosmosDB;
    private AzureOpenAIOperation aoaiOps;
	   
    public WorkersCompensationApplicationAnomaly (
    				CosmosDBOperation cosmosDB,
    				AzureOpenAIOperation aoaiOps
    			) {
		super();
		this.cosmosDB = cosmosDB;
		this.aoaiOps = aoaiOps;
	}

	@Override
	public String getAttachmentAnomaly(String attachmentId) {
  		String reviewMessage;
	  	String aedId = "";
	  	String aoaiReview = null;
	  	String attachmentName = null;
	  	
	  	String sqlStatement = "SELECT * FROM EmailExtracts e WHERE e.id = '" + attachmentId + "' " +
				" AND e.messageType IN ('email-attachment') ";
		
  		Iterator<AttachmentData> iterateAD = cosmosDB.query(sqlStatement, AttachmentData.class);
  		if (iterateAD == null) {
  			reviewMessage = String.format("Note: Attachment data not found in database. Attachment id %s", attachmentId);
  			logger.info(reviewMessage);
  			return reviewMessage;
  		}
  		// There should be only one instance in iterateAD
  		if (iterateAD.hasNext()) {
  			AttachmentData ad = iterateAD.next();
  			attachmentName = ad.getAttachmentName();
  			String messageId = ad.getMessageId();
  			
  			sqlStatement = "SELECT * FROM EmailExtracts e WHERE e.attachmentName = '" + attachmentName + "' " +
  							" AND e.messageType IN ('email-attachment-extracts') " + 
  							" AND e.messageId IN ('" + messageId + "') ";  			
  	  		Iterator<AttachmentExtractsData> iterateAED = cosmosDB.query(sqlStatement, AttachmentExtractsData.class);
  	  		if (iterateAED == null) {
  	  			reviewMessage = String.format("Note: Attachment extracted data not found in database. Attachment id %s", attachmentId);
  	  			logger.info(reviewMessage);
  	  			return reviewMessage;
  	  		}
  	  		// Make the AOAI call and let it find missing required fields		
  	  		Gson gson = new Gson();
  	  		AttachmentExtractsData aed = iterateAED.next();
  	  		
  	  		String jsonAttachmentData = gson.toJson(aed);
  	  		String quoteCleanedJson = StringUtils.replace(jsonAttachmentData, "\"", "'");
  	  		StringBuffer promptStringBuffer = new StringBuffer();
  	  		String promptStr = promptStringBuffer.append(FIND_MISSING_FIELDS_PROMPT).append(quoteCleanedJson).toString();
  	  		aoaiReview = aoaiOps.getAOAIChatCompletion(promptStr);
  	  		aedId = aed.getId();
  		}
		reviewMessage = String.format("Note: %s[%s] required field(s) check : %s", attachmentName, aedId, aoaiReview);
		logger.info(reviewMessage);
		return reviewMessage;
	}
}
