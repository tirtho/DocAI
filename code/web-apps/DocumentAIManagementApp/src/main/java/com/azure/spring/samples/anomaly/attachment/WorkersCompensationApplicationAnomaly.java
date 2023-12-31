package com.azure.spring.samples.anomaly.attachment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.cosmosdb.CosmosDBCommonQueries;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;

public class WorkersCompensationApplicationAnomaly implements AttachmentAnomaly {

    public static Logger logger = LoggerFactory.getLogger(WorkersCompensationApplicationAnomaly.class);
    
	public static String FIND_MISSING_FIELDS_PROMPT = "ApplicantName, "
														+ "ApplicantOfficePhone, "
														+ "ApplicantMobilePhone, "
														+ "ApplicantMailingAddress, "
														+ "and ApplicantEmailAddress "
														+ "are required fields in the json document below. \r\n"
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
	  	
	  	AttachmentExtractsData aed = CosmosDBCommonQueries.getAttachmentExtractedDataByAttachmentId (attachmentId, cosmosDB);
	  	if (aed == null) {
			reviewMessage = String.format("Note: Attachment extracted data not found");
			logger.info(reviewMessage);
			return reviewMessage;
	  	}
	  	reviewMessage = AttachmentAnomaly.attachmentExtractsDataRquiredFieldsReview(aed, aoaiOps, FIND_MISSING_FIELDS_PROMPT);
		logger.info(reviewMessage);
		
		// TODO: more reviews in future
		return reviewMessage;
	}
}
