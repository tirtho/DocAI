package com.azure.spring.samples.anomaly.attachment;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.samples.ai.AzureOpenAIOperation;
import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.cosmosdb.CosmosDBCommonQueries;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;

public class CommercialInsuranceApplicationAnomaly implements AttachmentAnomaly {

    public static Logger logger = LoggerFactory.getLogger(CommercialInsuranceApplicationAnomaly.class);
    
	public static String FIND_MISSING_FIELDS_PROMPT = "ApplicantName, "
														+ "ApplicantAddress, "
														+ "and ParentCompanyName "
														+ "are required fields in the json document below. \r\n"
														+ "Find which fields have no value. Answer in the format as "
														+ "'All required fields are present' or 'Required field(s) <field names> are absent' "
														+ "\n ";

    private CosmosDBOperation cosmosDB;
    private AzureOpenAIOperation aoaiOps;
	   
    public CommercialInsuranceApplicationAnomaly(
    				CosmosDBOperation cosmosDB,
    				AzureOpenAIOperation aoaiOps
    			) {
		super();
		this.cosmosDB = cosmosDB;
		this.aoaiOps = aoaiOps;
	}

    @Override
	public List<?> getAttachmentAnomaly(String attachmentId, String attachmentCategory) {
  		String reviewMessage;
		List<String> reviewSummary = new ArrayList<String>();
	  	
	  	AttachmentExtractsData aed = CosmosDBCommonQueries.getAttachmentExtractsDataByAttachmentId (attachmentId, cosmosDB);
	  	if (aed == null) {
			reviewMessage = String.format("Attachment extracted data not found");
			logger.info(reviewMessage);
			reviewSummary.add(reviewMessage);
			return reviewSummary;
	  	}
	  	
	  	reviewMessage = attachmentExtractsDataRquiredFieldsReview(aed, aoaiOps, FIND_MISSING_FIELDS_PROMPT);
		logger.info(reviewMessage);
		reviewSummary.add(reviewMessage);
		
		// TODO: more reviews in future
		
		return reviewSummary;
	}

}
