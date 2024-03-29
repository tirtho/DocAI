package com.azure.spring.samples.cosmosdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.spring.samples.adls.AzureADLSOperation;
import com.azure.spring.samples.ai.AzureAIOperation;
import com.azure.spring.samples.model.AttachmentData;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.model.EmailData;
import com.azure.spring.samples.utils.Category;
import com.azure.spring.samples.utils.ReturnEntity;

public class CosmosDBCommonQueries {

    public static Logger logger = LoggerFactory.getLogger(CosmosDBCommonQueries.class);

	public static AttachmentExtractsData getAttachmentExtractsDataByAttachmentId (String attachmentId, CosmosDBOperation cosmosDB) {
		
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
  			ReturnEntity<String, AttachmentExtractsData> response = getAttachmentExtractsDataByMessageIdAndAttachmentName (cosmosDB, messageId, attachmentName);
  			if (response != null) {
  				return response.getEntity();
  			}
  		}
		return null;
	}
	
	public static ReturnEntity<String, AttachmentExtractsData> getAttachmentExtractsDataByMessageIdAndAttachmentName(
											CosmosDBOperation cosmosDB,
											String messageId,
											String attachmentName
										) 
	{
		String reviewMessage = "";
		String sqlStatement = "SELECT * FROM EmailExtracts e WHERE e.attachmentName = '" + attachmentName + "' "
				+ " AND e.messageType IN ('email-attachment-extracts') " + " AND e.messageId IN ('" + messageId + "') ";
		Iterator<AttachmentExtractsData> iterateAED = cosmosDB.query(sqlStatement, AttachmentExtractsData.class);
		if (iterateAED == null || iterateAED.hasNext() == false) {
			reviewMessage = String.format("Attachment extracted data not found in database. Attachment name: %s, Message Id: %s",
					attachmentName, messageId);
			logger.info(reviewMessage);
			return null;
		}
		// TODO: Check if this is null. Could happen in extreme case.
		// If null, let the process continue
		AttachmentExtractsData aed = iterateAED.next();
		logger.info("Found attachment extracted data for attachment {}\n {}", attachmentName, aed);
		return new ReturnEntity<String, AttachmentExtractsData>(reviewMessage, aed);
	}
	
	public static <T> AttachmentExtractsData upsertAttachmentExtractedData(AttachmentExtractsData aed, CosmosDBOperation cosmosDB) {
		CosmosItemResponse<AttachmentExtractsData> response = cosmosDB.upsertAttachmentExtractsData(aed);
		int status = response.getStatusCode();
		logger.info("CosmosDB Upsert request status code %s", status);
		return response.getItem();
	}

	public static List<AttachmentData> getAttachmentDataListByEmailMessageId(CosmosDBOperation cosmosDB, String messageId) {
		String sqlStatement = "SELECT * FROM EmailExtracts e WHERE e.messageId IN ('" + messageId + "') "
								+ " AND e.messageType IN ('email-attachment') ";
  		Iterator<AttachmentData> iterateAD = cosmosDB.query(sqlStatement, AttachmentData.class);
  		if (iterateAD != null) {
  	  		List<AttachmentData> adl = new ArrayList<>();
  			while(iterateAD.hasNext()) {
  				AttachmentData ad = iterateAD.next();
  				adl.add(ad);
  			}
  			return adl;
  		}
  		return null;
	}
	
	public static String deleteMessageWithDependecies(CosmosDBOperation cosmosDB, AzureAIOperation aiOps, AzureADLSOperation adlsOps, String id) {
		String errorResponse = String.format("Failed to delete email message and all dependencies by id: %s", id);
		
		// Get the email item
		EmailData ed = CosmosDBCommonQueries.getItemById(cosmosDB, EmailData.class, id);
		int deletedStatus = HttpStatus.SC_NO_CONTENT;
		int status;
		if (ed != null) {
			List<AttachmentData> adl = CosmosDBCommonQueries.getAttachmentDataListByEmailMessageId(cosmosDB, ed.getMessageId());
			if (adl != null) {
				for (AttachmentData ad : adl) {
					ReturnEntity<String, AttachmentExtractsData> aedReturnEntity = CosmosDBCommonQueries.getAttachmentExtractsDataByMessageIdAndAttachmentName(
																			cosmosDB, 
																			ed.getMessageId(), 
																			ad.getAttachmentName()
																		);
					if (ad.getCategories() != null) {
						List<String> attachmentCategories = Category.getCategoryList(ad.getCategories());
						for (String category : attachmentCategories) {
							if (category.startsWith("video-")) {
								// Need to also delete the video ingestion index
								AttachmentExtractsData aed = aedReturnEntity.getEntity();
								String videoIndex = aed.getFieldValue("VideoDocumentId");
								if (videoIndex != null) {
									status = aiOps.deleteVideoIndex(videoIndex);
									if (status != HttpStatus.SC_NO_CONTENT) {
										logger.info("Failed to delete video index id: {}", videoIndex);
										deletedStatus = status;
									}
								}
							}
						}
					}
					String aedId = aedReturnEntity.getEntity().getId();

					status = cosmosDB.delete(aedReturnEntity.getEntity());
					if (status != HttpStatus.SC_NO_CONTENT) {
						logger.info("Failed to delete AttachmentExtractsData with id: {}", aedId);
						deletedStatus = status;
					}

					String adId = ad.getId();
					status = cosmosDB.delete(ad);
					if (status != HttpStatus.SC_NO_CONTENT) {
						logger.info("Failed to delete AttachmentData with id: {}", adId);
						deletedStatus = status;
					}
				}
			}
			// Get the parent folder of the email message body and delete that folder from blob store
			String urlToRemove = StringUtils.removeEnd(ed.getUrl(), "EmailBody.msg");
			try {
				status = adlsOps.deleteBlob(urlToRemove);
				if (status != HttpStatus.SC_ACCEPTED && status != HttpStatus.SC_OK) {
					logger.info("Failed to delete Blob Storage folder: {}", urlToRemove);
					deletedStatus = HttpStatus.SC_BAD_REQUEST;
				}
			} catch (Exception e) {
				logger.info("Delete Blob Storage folder: {} raised exception: {}", urlToRemove, e);
				deletedStatus = HttpStatus.SC_BAD_REQUEST;
			}

			status = cosmosDB.delete(ed);
			if (deletedStatus != HttpStatus.SC_NO_CONTENT) {
				logger.info("Failed to delete EmailData with id: {}", id);
				deletedStatus = status;
			}
			 
		}
		
		if (deletedStatus != HttpStatus.SC_NO_CONTENT) 
			return errorResponse;
		else 
			return String.format("Success: Deleted email message and all dependencies by id: %s", id);
	}
	
	public static <T> T getItemById(CosmosDBOperation cosmosDB, Class<T> itemClass, String id) {
		StringBuffer sqlBuff = new StringBuffer("SELECT * FROM EmailExtracts e WHERE e.id IN ('" + id + "') ");
		Iterator<T> itr = cosmosDB.query(sqlBuff.toString(), itemClass);
		if (itr == null) {
			logger.info("Item not found for id : {}", id);
			return null;
		}
  		while (itr.hasNext()) {
  			// There should be only one
  			T ed = itr.next();
  			logger.info("Item found : {}", ed.toString());
  			return ed;
  		}
		logger.info("From iterator, item not found for id : {}", id);
		return null;
	}
}
