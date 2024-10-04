package com.azure.spring.samples.anomaly.attachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.cosmosdb.CosmosDBCommonQueries;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;

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
													+ "Find which of the above required fields have no value in the json document below. Answer in the format as "
													+ "'All required fields are present' or 'Required field(s) <field names> are absent' "
													+ "\n ";

	public static String FIND_SUSTAINED_INJURY_WHEN_NOT_IN_CAR_PROMPT = 
							"From the INCIDENT_DESCRIPTION field find if the person was inside the car. \n"
							+ "From the SUSTAINED_INJURIES field find if there was any bodily injury. \n"
							+ "INCIDENT_DESCRIPTION: %s\n"
							+ "SUSTAINED_INJURIES: %s\n"
							+ "Answer in json format { 'insideCar': 'yes/no', 'bodilyInjury': 'yes/no' } \n"
							+ "Return the json data only.";

	public static String INCIDENT_TIME_FIELD_NAME = "IncidentTime";
	public static String INCIDENT_LOCATION_FIELD_NAME = "IncidentLocation";
	public static String FIELDS_FOR_INCIDENT_TIME_LOCATION_MATCH_CHECK = INCIDENT_TIME_FIELD_NAME + ", " + INCIDENT_LOCATION_FIELD_NAME;
			
	public static String FIND_US_LOCATION_DATE_TIME_MATCH_PROMPT = 
				"Find the 3 character timezone from \n"
				+ "ADDRESS: %s\n"
				+ "as addressTimezone.\n"
				+ "Find the 3 character timezone from \n"
				+ "TIME: %s\n"
				+ "as incidentTimezone.\n"
				+ "If incidentTimezone and addressTimezone are same, then\r\n"
				+ "isMatching = true otherwise it should be false.\n"
				+ "Return incidentTimezone, addressTimezone and isMatching in json data only.";
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
	public List<?> getAttachmentAnomaly(String attachmentId, String attachmentCategory) {
  		String reviewMessage;
		List<String> reviewSummary = new ArrayList<String>();
  		
  		// Check for required field
	  	AttachmentExtractsData aed = CosmosDBCommonQueries.getAttachmentExtractsDataByAttachmentId (attachmentId, cosmosDB);
	  	if (aed == null) {
			reviewMessage = String.format("Note:<br>Attachment extracted data not found");
			logger.info(reviewMessage);
			reviewSummary.add(reviewMessage);
			return reviewSummary;
	  	}
	  	
	  	reviewMessage = attachmentExtractsDataRquiredFieldsReview(aed, aoaiOps, FIND_MISSING_FIELDS_PROMPT);
		logger.info(reviewMessage);
		reviewSummary.add(String.format("Missing Fields: %s", reviewMessage));
		
		// Check for fraud or field errors
		String aoaiFraudReview = getFraudCheckResult(aed);
  		logger.info(aoaiFraudReview);
		reviewSummary.add(String.format("Fraud Review: %s", aoaiFraudReview));
  		
  		// Check for data inconsistencies and fix those, if possible, based on surrounding context
  		String aoaiDataQualityReview = getDateLocationCheckResult(aed);
  		logger.info(aoaiDataQualityReview);
		reviewSummary.add(String.format("Data Inconsistency Review: %s", aoaiDataQualityReview));

		// TODO: more reviews in future
		
		
  		return reviewSummary;	
	}
	
	private String getDateLocationCheckResult(AttachmentExtractsData aed) {
		
		Map<String, String> checkingFields = searchFieldValueMapFromAttachmentExtractsData(aed, FIELDS_FOR_INCIDENT_TIME_LOCATION_MATCH_CHECK);

		String prompt = String.format(
										FIND_US_LOCATION_DATE_TIME_MATCH_PROMPT,
										checkingFields.get(INCIDENT_LOCATION_FIELD_NAME),
										checkingFields.get(INCIDENT_TIME_FIELD_NAME)
									);
		logger.info("Checking date location mismatch with prompt : {}", prompt);
		
		return aoaiOps.getAOAIChatCompletion(prompt);
	}

	private String getFraudCheckResult(AttachmentExtractsData aed) {
		// Check for fraud or any discrepancy	
		Map<String, String> fraudCheckFields = searchFieldValueMapFromAttachmentExtractsData(aed, FIELDS_FOR_FRAUD_DETECTION);
		
		String prompt = String.format(FIND_SUSTAINED_INJURY_WHEN_NOT_IN_CAR_PROMPT, 
				fraudCheckFields.get(INCIDENT_DESCRIPTION_FIELD_NAME),
				fraudCheckFields.get(SUSTAINED_INJURIES_FIELD_NAME));
		logger.info("Checking fraud with prompt : {}", prompt);
  		return aoaiOps.getAOAIChatCompletion(prompt);
	}

}