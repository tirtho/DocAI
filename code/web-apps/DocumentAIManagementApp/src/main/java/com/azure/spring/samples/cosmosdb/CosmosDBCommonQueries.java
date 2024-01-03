package com.azure.spring.samples.cosmosdb;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.samples.model.AttachmentData;
import com.azure.spring.samples.model.AttachmentExtractsData;

public class CosmosDBCommonQueries {

    public static Logger logger = LoggerFactory.getLogger(CosmosDBCommonQueries.class);

	public static AttachmentExtractsData getAttachmentExtractedDataByAttachmentId (String attachmentId, CosmosDBOperation cosmosDB) {
		
  		String reviewMessage;
	  	String attachmentName = null;

	  	String sqlStatement = "SELECT * FROM EmailExtracts e WHERE e.id = '" + attachmentId + "' " +
				" AND e.messageType IN ('email-attachment') ";
		
  		Iterator<AttachmentData> iterateAD = cosmosDB.query(sqlStatement, AttachmentData.class);
  		if (iterateAD == null) {
  			reviewMessage = String.format("Attachment data not found in database. Attachment id %s", attachmentId);
  			logger.info(reviewMessage);
  			return null;
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
  	  			reviewMessage = String.format("Attachment extracted data not found in database. Attachment id %s", attachmentId);
  	  			logger.info(reviewMessage);
  	  			return null;
  	  		}
  	  		AttachmentExtractsData aed = iterateAED.next();
  	  		logger.info("Found attachment extracted data for attachment {}\n {}", ad.getAttachmentName(), aed);
  	  		return aed;
  		}

		return null;
	}
}
