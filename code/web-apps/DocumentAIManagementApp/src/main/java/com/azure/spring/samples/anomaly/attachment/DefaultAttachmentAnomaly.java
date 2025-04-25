package com.azure.spring.samples.anomaly.attachment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.ai.AzureCUOperation;
import com.azure.spring.samples.ai.AzureOpenAIOperation;
import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.cosmosdb.CosmosDBCommonQueries;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.model.ExtractData;
import com.azure.spring.samples.utils.Category;
import com.azure.spring.samples.utils.FileType;
import com.azure.spring.samples.utils.ReturnEntity;
import com.azure.spring.samples.utils.Transform;

public class DefaultAttachmentAnomaly implements AttachmentAnomaly {

    public static Logger logger = LoggerFactory.getLogger(DefaultAttachmentAnomaly.class);
    
    private static String AOAI_OMNI_PROMPT = 
					"{"
					+ "    \"messages\": [ \r\n"
					+ "        { \"role\": \"system\", \"content\": \"You are a helpful assistant.\" }, \r\n"
					+ "        { \"role\": \"user\", \r\n"
					+ "        \"content\": [  \r\n"
					+ "            { \r\n"
					+ "                \"type\": \"text\", \r\n"
					+ "                \"text\": \"%s\" \r\n"
					+ "            },\r\n"
					+ "            { \r\n"
					+ "                \"type\": \"image_url\", \r\n"
					+ "                \"image_url\": {\r\n"
					+ "                    \"url\" : \"%s\"\r\n"
					+ "                }\r\n"
					+ "            }\r\n"
					+ "        ]} \r\n"
					+ "    ], \r\n"
					+ "	   \"temperature\": 0.0,\r\n"
					+ "    \"max_tokens\": 4096 \r\n"
					+ "}   \r\n";
        
    private static String MEDICAL_REVIEW_USER_PROMPT = 
    		"List the data from the document as key/value pair in a JSON format.";
       
	private static String CAR_HOME_IMAGE_REVIEW_PROMPT = 
			"Identify image is a CAR, HOME or OTHER. Detect any damage to the car or home, else say NONE. "
			+ "Also return any text detected in the image. Return JSON data ONLY in the format:"
			+ "{ "
			+ "'imageOf': 'CAR or HOME or OTHER', "
			+ "'damageAssessment': [list all damaged, discolored, broken and missing items in the image of the car or home. List NONE if no damaged items found.], "
			+ "'surroundings': <any location or surrounding or background information>,"
			+ " 'anyText': <return any text detected in the image>"
			+ "}";
	
	private static String FIND_VIDEO_REVIEW_PROMPT = 
			"From the EXTRACTED_DATA field find if there is any anomaly \n"
			+ "EXTRACTED_DATA: %s\n"
			+ "Answer in json format { 'anyAomaly': 'yes/no', 'anomalyDetails': <describe the detected anomaly in less than 100 words> } \n"
			+ "Return the json data only.";
	
    private CosmosDBOperation cosmosDB;
    private AzureOpenAIOperation aoaiOps;
    private AzureCUOperation cuOps;
	
    /**
     * 
     * @param cosmosDB
     * @param aoaiVisionOps
     * @param aiOps
     * @param blobStoreSASToken
     */
    public DefaultAttachmentAnomaly(
    				CosmosDBOperation cosmosDB,
    				AzureOpenAIOperation aoaiOps,
    				AzureCUOperation cuOps
    			) {
		super();
		this.cosmosDB = cosmosDB;
		this.aoaiOps = aoaiOps;
		this.cuOps = cuOps;
	}

    /**
     * Default is where the document does not belong to a 
     * certain class in Form Recognizer. It is most likely one of - 
     * a. An Image File
     * b. A Video
     * c. A generic document of the type not supported by Form Recognizer
     * 
     * Note - 
     * 		Form Recognizer supports JPEG, PNG, BMP, TIFF, HEIF, PDF
     * 		GPT4 Vision supports JPEG, GIF, PNG and WEBP and MP4 and MOV
     * 
     */
	@Override
	public List<?> getAttachmentAnomaly(String attachmentId, String attachmentCategory) {

		List<String> reviewSummary = new ArrayList<>();
		String reviewMessage = null;

	  	AttachmentExtractsData aed = CosmosDBCommonQueries.getAttachmentExtractsDataByAttachmentId (attachmentId, cosmosDB);
	  	String fileUrl = aed.getUrl(); 
	  	// If GPT-4o supports MI for accessing blob store 
	  	// we do not need the blob store SAS token
	  	// StringUtils.replace(String.format("%s%s", aed.getUrl(), blobStoreSASToken), "%%", "%");
	  	
	  	String filename = aed.getAttachmentName();
		// TODO: Check if text or image or video by the content of the file and not just by the file extension
		// Get url of image/video file
		// If image/video, pass to AOAI GPT-4o / GPT-4 Vision API with enhancements
		// Else read content and pass to regular AOAI API

	  	FileType ft = FileType.fromFilename(filename);	
	  	if (ft.isImage()) {
	  		// Pass prompt for image for GPT-4 Omni API
	  		String prompt = null;	  		

	  		// Detect the image category and then generate the right prompt
	  		List<String> attachmentCategories = Arrays.asList(attachmentCategory);
	  		
	  		// For Auto and Home
	  		List<String> carHomeCategory = new ArrayList<>();
	  		carHomeCategory.add("image-automobile");
	  		carHomeCategory.add("image-home");
	  		// For Medical
	  		List<String> medicalCategory = new ArrayList<>();
	  		medicalCategory.add("image-medical-rx-note");
	  		medicalCategory.add("image-medical-lab-report");
	  			  		
	  		if (Category.hasAny(carHomeCategory, attachmentCategories)) {
	  			prompt = String.format(AOAI_OMNI_PROMPT, CAR_HOME_IMAGE_REVIEW_PROMPT, fileUrl);
		  		logger.info(prompt);	  			
		  		reviewMessage = String.format("Image Review: %s", aoaiOps.getAOAIOmniCompletion(prompt, false));
	  		} else if (Category.hasAny(medicalCategory, attachmentCategories)) {
	  			prompt = String.format(AOAI_OMNI_PROMPT, MEDICAL_REVIEW_USER_PROMPT, fileUrl);
		  		logger.info(prompt);	  			
		  		reviewMessage = String.format("Image Review: %s", aoaiOps.getAOAIOmniCompletion(prompt, false));
	  		} else {
				reviewMessage = String.format("Image Review: Image category unknown.");
	  		}	  		
	  	} else if (ft.isAudio()) {
	  		// TODO: pass prompt for audio
	  	} else if (ft.isVideo()) {
	  		// Just passing the extracted json to LLM for completion
	  		// No need to send the video file
	  		reviewMessage = processVideoReview(aed);
	  	} else if (ft.isText()) {
	  		// TODO: pass prompt for text
	  	} else if (ft.isDocument()) {
	  		// TODO: pass prompt for docs like word or pdf etc..
	  	}
	  	
  		logger.info("Default Anomaly returned response: %s", reviewMessage);
  		reviewSummary.add(reviewMessage);
		return reviewSummary;
	}

	/**
	 * 
	 * @param aed
	 * @param extract
	 * @param videoDocumentIdField
	 * @param ingestionStatusField
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String processVideoReview(
					AttachmentExtractsData aed
					) {
		
		if (aed.getOperationId() != null && aed.getOperationStatus() != null) {
			// If status was already 'Succeeded' in cosmosdb 
			// Then call GPT-4 and get review
			if (StringUtils.compare(aed.getOperationStatus().toLowerCase(), "succeeded") == 0) {
				// Call GPT4 for review
				String extractedVideoDataInJson = Transform.attachmentExtractsDataToJson(aed);
		  		String prompt = String.format(FIND_VIDEO_REVIEW_PROMPT, extractedVideoDataInJson);
				String message = aoaiOps.getAOAIChatCompletion(prompt);
				logger.info(message);
				return message;
			} else if (StringUtils.compare(aed.getOperationStatus().toLowerCase(), "running") == 0) {
				String message = String.format("OperationId[%s] ingestion not complete yet;", aed.getOperationId());
				logger.info(message);
				return message;
			} else {
				// Status is neither Running nor Completed, nor was it completed via above code
				String message = String.format("OperationId[%s]: Video ingestion and getting extacts did not succeed. Need human review", aed.getOperationId());
				logger.info(message);
				return message;
			}
		} else {
			return "Video extract not found";
		}
	}
		
	@SuppressWarnings("unchecked")
	public <U, V> ReturnEntity<U, V> updateVideoExtractionData(String attachmentId) {
	  	AttachmentExtractsData aed = CosmosDBCommonQueries.getAttachmentExtractsDataByAttachmentId (attachmentId, cosmosDB);
	  	// Content Understanding API does not need to access the actual file
	  	// to get the result of extraction. You just need to pass the operationId
	  	// So, no need anymore for the file url with the SAS token
	  	// String fileUrl = aed.getUrl() + "?" + blobStoreSASToken; 
	  	//StringUtils.replace(String.format("%s%s", aed.getUrl(), blobStoreSASToken), "%%", "%");
  		String operationId = aed.getOperationId();
  		String ingestionState = aed.getOperationStatus();
		String message = null;
		
  		List<ExtractData> extracts = aed.getExtracts();
  		if (extracts == null) 
  			extracts = new ArrayList<>();
  		if (extracts.isEmpty())
  			extracts.add(new ExtractData());

		for (ExtractData extract : extracts) {
  			if (operationId != null && ingestionState != null) {
  				// If it was running during last call 
  				// Or you may have deleted it in cosmos Db and now want to get it
  				// again from CU before it is deleted from CU.
  				if (StringUtils.compare(ingestionState.toLowerCase(), "running") == 0 || 
  						StringUtils.compare(ingestionState.toLowerCase(), "succeeded") == 0) {
  					// Use Content Understanding to extract information about video
  					ReturnEntity<String, ?> extractedEntity = cuOps.getVideoExtractionResult(operationId);
  					List<Map<String, ?>> updatedFields = (List<Map<String, ?>>) extractedEntity.getEntity();
  					String extractionStatus = extractedEntity.getStatus();
  					aed.setOperationStatus(extractionStatus);
  					
  					if (updatedFields != null) {
  	  					extract.upsertFields(updatedFields);
  	  					// Update the fields for this attachment in cosmosdb
  	  					CosmosDBCommonQueries.upsertAttachmentExtractedData(aed, cosmosDB);
  	  					message = String.format("Success: OperationId[%s] fields updated;", operationId);
  	  					logger.info(message);
  					} else {
  	  					message = String.format("Warning: OperationId[%s] CU call got fields returned/parsed as null. Cosmos DB not updated;", operationId);
  	  					logger.info(message);
  					}
  				} else {
  					message = String.format("Success: OperationId[%s] ingestion not complete yet;", operationId);
  					logger.info(message);
  				}
  			} else {
  				message = "Error: Video extract not found";
  				logger.info(message);
  				return (ReturnEntity<U, V>) new ReturnEntity<String, List<ExtractData>>(message, extracts);  				
  			}
		}
		return (ReturnEntity<U, V>) new ReturnEntity<String, List<ExtractData>>(message, extracts);
	}
}
