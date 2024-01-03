package com.azure.spring.samples.anomaly.attachment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.cosmosdb.CosmosDBCommonQueries;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.model.ExtractData;

public class AutoInsuranceClaimAnomaly implements AttachmentAnomaly {
	
    public static Logger logger = LoggerFactory.getLogger(AutoInsuranceClaimAnomaly.class);

	public static String INCIDENT_DESCRIPTION_FIELD_NAME = "IncidentDescription";
	public static String SUSTAINED_INJURIES_FIELD_NAME = "SustainedInjuries";

    public static String FIELDS_FOR_FRAUD_DETECTION = 
    									"IncidentTime, "
										+ "IncidentLocation, "
										+ INCIDENT_DESCRIPTION_FIELD_NAME + ", "
										+ "VehicleMakeAndModel, "
										+ "VehicleDamageExtent, "
										+ SUSTAINED_INJURIES_FIELD_NAME + ", "
										+ "AdditionalComments";

    
	public static String FIND_MISSING_FIELDS_PROMPT = FIELDS_FOR_FRAUD_DETECTION
													+ ", "
													+ "Name, "
													+ "Address, "
													+ "Phone, "
													+ "and Email "
													+ "are required fields in the json document below. \r\n"
													+ "Find which fields have no value. Answer in the format as "
													+ "'All required fields are present' or 'Required field(s) <field names> are absent' "
													+ "\n ";

	public static String FIND_SUSTAINED_INJURY_WHEN_NOT_IN_CAR = 
							"From the INCIDENT_DESCRIPTION field find if the person was inside the car. \n"
							+ "From the SUSTAINED_INJURIES field find if there was any bodily injury. \n"
							+ "INCIDENT_DESCRIPTION: %s\n"
							+ "SUSTAINED_INJURIES: %s\n"
							+ "Answer in json format { 'insideCar': 'yes/no', 'bodilyInjury': 'yes/no' } \n"
							+ "Return the json data only.";
	
	
	private CosmosDBOperation cosmosDB;
    private AzureOpenAIOperation aoaiOps;
	   
    public AutoInsuranceClaimAnomaly (
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
	  	
  		// Check for required field
	  	AttachmentExtractsData aed = CosmosDBCommonQueries.getAttachmentExtractedDataByAttachmentId (attachmentId, cosmosDB);
	  	if (aed == null) {
			reviewMessage = String.format("Note: Attachment extracted data not found");
			logger.info(reviewMessage);
			return reviewMessage;
	  	}
	  	reviewMessage = AttachmentAnomaly.attachmentExtractsDataRquiredFieldsReview(aed, aoaiOps, FIND_MISSING_FIELDS_PROMPT);
		logger.info(reviewMessage);
		
		// Check for fraud or any discrepancy
		List<String> fraudFieldNameArray = Arrays.asList(StringUtils.split(FIELDS_FOR_FRAUD_DETECTION, ", "));
		
		Map<String, String> fraudCheckFields = new HashMap<>();
		// TODO: more reviews in future
		for (ExtractData ed : aed.getExtracts()) {
			for (Map<String, ?> field : ed.getFields()) {
				String fieldName = (String) field.get("fieldName");
				if (fraudFieldNameArray.contains(fieldName)) {
					String fieldValue = String.format("%s", field.get("fieldValue"));
					fraudCheckFields.put(fieldName, fieldValue);
				}
			}
		}
		String prompt = String.format(FIND_SUSTAINED_INJURY_WHEN_NOT_IN_CAR, 
				fraudCheckFields.get(INCIDENT_DESCRIPTION_FIELD_NAME),
				fraudCheckFields.get(SUSTAINED_INJURIES_FIELD_NAME));
		logger.info("Checking fraud with prompt : {}", prompt);
  		String aoaiReview = aoaiOps.getAOAIChatCompletion(prompt);
  		
  		logger.info(aoaiReview);
  		
  		return String.format("%s; Fraud Review: %s", reviewMessage, aoaiReview);	
	}

}