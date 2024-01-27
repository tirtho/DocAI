package com.azure.spring.samples.anomaly.attachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.ai.AzureAIOperation;
import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.cosmosdb.CosmosDBCommonQueries;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.model.ExtractData;
import com.azure.spring.samples.utils.FileType;

public class DefaultAttachmentAnomaly implements AttachmentAnomaly {

    public static Logger logger = LoggerFactory.getLogger(DefaultAttachmentAnomaly.class);
    
	private static String FIND_IMAGE_ENHANCEMENTS_REVIEW_PROMPT = 
			"{"
			+ "    \"enhancements\": {\r\n"
			+ "        \"ocr\": {\r\n"
			+ "          \"enabled\": true\r\n"
			+ "        },\r\n"
			+ "        \"grounding\": {\r\n"
			+ "          \"enabled\": true\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"dataSources\": [\r\n"
			+ "    {\r\n"
			+ "        \"type\": \"AzureComputerVision\",\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"endpoint\": \"%s\",\r\n"
			+ "            \"key\": \"%s\"\r\n"
			+ "        }\r\n"
			+ "    }],\r\n"
			+ "    \"messages\": [ \r\n"
			+ "        { \"role\": \"system\", \"content\": \"You are a helpful assistant.\" }, \r\n"
			+ "        { \"role\": \"user\", \r\n"
			+ "        \"content\": [  \r\n"
			+ "            { \r\n"
			+ "                \"type\": \"text\", \r\n"
			+ "                \"text\": \"Identify image is a CAR, HOME or OTHER. Detect any damage to the car or home, else say NONE. Also return any text detected in the image. Return JSON data ONLY in the format:"
			+ "							 { 'imageOf': 'CAR or HOME or OTHER', 'damageAssessment': [list all damaged, discolored, broken and missing items in the image of the car or home. List NONE if no damaged items found.], 'surroundings': <any location or surrounding or background information>, 'anyText': <return any text detected in the image>}\" \r\n"
			+ "            },\r\n"
			+ "            { \r\n"
			+ "                \"type\": \"image_url\", \r\n"
			+ "                \"image_url\": {\r\n"
			+ "                    \"url\" : \"%s\"\r\n"
			+ "                }\r\n"
			+ "            }\r\n"
			+ "        ]} \r\n"
			+ "    ], \r\n"
			+ "	   \"stream\": false,\r\n"
			+ "	   \"temperature\": 0.0,\r\n"
			+ "    \"max_tokens\": 4096 \r\n"
			+ "}   \r\n";
	
	private static String FIND_VIDEO_ENHANCEMENTS_REVIEW_PROMPT = 
			"{\r\n"
			+ "    \"enhancements\": {\r\n"
			+ "        \"video\": {\r\n"
			+ "          \"enabled\": true\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"dataSources\": [\r\n"
			+ "    {\r\n"
			+ "        \"type\": \"AzureComputerVisionVideoIndex\",\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"computerVisionBaseUrl\": \"%scomputervision\",\r\n"
			+ "            \"computerVisionApiKey\": \"%s\",\r\n"
			+ "            \"indexName\": \"%s\",\r\n"
			+ "            \"videoUrls\": [\"%s\"]\r\n"
			+ "        }\r\n"
			+ "    }],\r\n"
			+ "    \"messages\": [ \r\n"
			+ "        { \"role\": \"system\", \"content\": \"You are a helpful assistant.\" }, \r\n"
			+ "        { \"role\": \"user\", \r\n"
			+ "        \"content\": [  \r\n"
			+ "            { \r\n"
			+ "                \"type\": \"acv_document_id\", \r\n"
			+ "                \"acv_document_id\": \"%s\" \r\n"
			+ "            }, \r\n"
			+ "            { \r\n"
			+ "                \"type\": \"text\", \r\n"
			+ "                \"text\": \"Identify image is a CAR, HOME or OTHER. Detect any damage to the car or home, else say NONE. Also return any text detected in the image. Return JSON data ONLY in the format:"
			+ "							 { 'imageOf': 'CAR or HOME or OTHER', 'damageAssessment': [list all damaged, discolored, broken and missing items in the image of the car or home. List NONE if no damaged items found.], 'surroundings': <any location or surrounding or background information>, 'anyText': <return any text detected in the image>}\" \r\n"
			+ "            } \r\n"
			+ "        ]}\r\n"
			+ "    ], \r\n"
			+ "	   \"stream\": false,\r\n"
			+ "	   \"temperature\": 0.0,\r\n"
			+ "    \"max_tokens\": 4096 \r\n"
			+ "}   \r\n";

	private static String FIND_VIDEO_ENHANCEMENTS_DESCRIPTION_PROMPT = 
			"{\r\n"
			+ "    \"enhancements\": {\r\n"
			+ "        \"video\": {\r\n"
			+ "          \"enabled\": true\r\n"
			+ "        }\r\n"
			+ "    },\r\n"
			+ "    \"dataSources\": [\r\n"
			+ "    {\r\n"
			+ "        \"type\": \"AzureComputerVisionVideoIndex\",\r\n"
			+ "        \"parameters\": {\r\n"
			+ "            \"computerVisionBaseUrl\": \"%scomputervision\",\r\n"
			+ "            \"computerVisionApiKey\": \"%s\",\r\n"
			+ "            \"indexName\": \"%s\",\r\n"
			+ "            \"videoUrls\": [\"%s\"]\r\n"
			+ "        }\r\n"
			+ "    }],\r\n"
			+ "    \"messages\": [ \r\n"
			+ "        { \"role\": \"system\", \"content\": \"You are a car insurance and accident expert.\" }, \r\n"
			+ "        { \"role\": \"user\", \r\n"
			+ "        \"content\": [  \r\n"
			+ "            { \r\n"
			+ "                \"type\": \"acv_document_id\", \r\n"
			+ "                \"acv_document_id\": \"%s\" \r\n"
			+ "            }, \r\n"
			+ "            { \r\n"
			+ "                \"type\": \"text\", \r\n"
			+ "                \"text\": \"Describe this video.\" \r\n"
			+ "            } \r\n"
			+ "        ]}\r\n"
			+ "    ], \r\n"
			+ "	   \"stream\": false,\r\n"
			+ "	   \"temperature\": 0.0,\r\n"
			+ "    \"max_tokens\": 4096 \r\n"
			+ "}   \r\n";
	
    private CosmosDBOperation cosmosDB;
    private AzureOpenAIOperation aoaiVisionOps;
    private AzureAIOperation aiOps;
    private String blobStoreSASToken;
	
    /**
     * 
     * @param cosmosDB
     * @param aoaiVisionOps
     * @param aiOps
     * @param blobStoreSASToken
     */
    public DefaultAttachmentAnomaly(
    				CosmosDBOperation cosmosDB,
    				AzureOpenAIOperation aoaiVisionOps,
    				AzureAIOperation aiOps,
    				String blobStoreSASToken
    			) {
		super();
		this.cosmosDB = cosmosDB;
		this.aoaiVisionOps = aoaiVisionOps;
		this.aiOps = aiOps;
		this.blobStoreSASToken = blobStoreSASToken;
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
	public List<?> getAttachmentAnomaly(String attachmentId) {

		List<String> reviewSummary = new ArrayList<>();
		String reviewMessage = null;

	  	AttachmentExtractsData aed = CosmosDBCommonQueries.getAttachmentExtractedDataByAttachmentId (attachmentId, cosmosDB);
	  	String fileUrl = StringUtils.replace(String.format("%s%s", aed.getUrl(), blobStoreSASToken), "%%", "%");
	  	String filename = aed.getAttachmentName();
		// TODO: Check if text or image or video by the content of the file and not just by the file extension
		// Get url of image/video file
		// If image/video, pass to AOAI Vision API with enhancements
		// Else read content and pass to regular AOAI API

	  	FileType ft = FileType.fromFilename(filename);	
	  	if (ft.isImage()) {
	  		// pass prompt for image for GPT 4 Vision API
	  		//String prompt = String.format(FIND_IMAGE_PROMPT, fileUrl);
	  		// Use the GPT4 Vision API with Image Enhancement with Azure Vision API
	  		String prompt = String.format(FIND_IMAGE_ENHANCEMENTS_REVIEW_PROMPT, aiOps.getAiEndpoint(), aiOps.getAiKey(), fileUrl);
	  		logger.info(prompt);
	  		reviewMessage = String.format("Image Review: %s", aoaiVisionOps.getAOAIVisionCompletion(prompt, true));
	  	} else if (ft.isAudio()) {
	  		// TODO: pass prompt for audio
	  	} else if (ft.isVideo()) {
	  		// Check if the video ingestion status from 
	  		// the form extract data.
	  		// If state is 'failed', return 'ingestion failed'
	  		// If state is 'running', make the GET ingestion status call
	  		// and update document extract item in cosmosdb
	  		// If state is 'completed', make the GPT4 VISION with video enhancement call
	  		List<ExtractData> extracts = aed.getExtracts();
	  		StringBuffer messageBuffer = new StringBuffer("Video Review: ");
  			Map<String, ?> ingestionStatusField = null;
  			Map<String, ?> videoDocumentIdField = null;
	  		for (ExtractData extract : extracts) {
	  			for (Map<String,?> field : extract.getFields()) {
	  				if (StringUtils.compare("VideoDocumentId", (String) field.get("fieldName")) == 0) {
	  					videoDocumentIdField = field;
	  				} else if (StringUtils.compare("IngestionStatus", (String) field.get("fieldName")) == 0) {
	  					ingestionStatusField = field;
	  				}
	  			}
	  			String review = processVideoReview(fileUrl, aed, extract, videoDocumentIdField, ingestionStatusField);
	  			messageBuffer.append(review);
	  		}
	  		reviewMessage = messageBuffer.toString();
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
	public String processVideoReview(
					String fileUrl,
					AttachmentExtractsData aed, 
					ExtractData extract, 
					Map<String, ?> videoDocumentIdField,
					Map<String, ?> ingestionStatusField
				) {
		
		String videoDocumentId = (String) videoDocumentIdField.get("fieldValue");
		String ingestionState = (String) ingestionStatusField.get("fieldValue");
		if (videoDocumentId != null && ingestionState != null) {
			boolean isCompleted = false;
			if (StringUtils.compare(ingestionState.toLowerCase(), "running") == 0) {
				// Time to make another getIngestionState() call to update status
				String state = aiOps.getVideoIngestionState(videoDocumentId);
				if (StringUtils.compare(state.toLowerCase(), "completed") == 0) {
					isCompleted = true;
					// Use GPT4 Vision to get a Description of the video
			  		String prompt = String.format(FIND_VIDEO_ENHANCEMENTS_DESCRIPTION_PROMPT,
								aiOps.getAiEndpoint(), 
								aiOps.getAiKey(), 
								videoDocumentId, 
								fileUrl,
								videoDocumentId
							);
			  		// Call GPT4 Vision to get description of the video
			  		String description = aoaiVisionOps.getAOAIVideoCompletion(prompt);
			  		
			  		// Now update the status and description fields in cosmosdb
			  		List<Map<String, ?>> updatedFields = new ArrayList<>();
			  		Map descriptionField = new HashMap<>();
			  		descriptionField.put("fieldName", "Description");
			  		descriptionField.put("fieldValueType", "string");
			  		descriptionField.put("fieldConfidence", 0.99);
			  		descriptionField.put("fieldValue", description);
			  		updatedFields.add((HashMap<String, ?>) descriptionField);
			  		
			  		Map statusField = new HashMap<>();
			  		statusField.put("fieldName", "IngestionStatus");
			  		statusField.put("fieldValueType", "string");
			  		statusField.put("fieldConfidence", 0.99);
			  		statusField.put("fieldValue", "Completed");
			  		updatedFields.add((HashMap<String, ?>) statusField);
			  		extract.upsertFields(updatedFields);
					// Update the 'IngestionStatus' and 'Description' fields for this attachment in cosmosdb
					CosmosDBCommonQueries.upsertAttachmentExtractedData(aed, cosmosDB);
					
					String message = String.format("Document[%s] status and description fields updated;", videoDocumentId);
					logger.info(message);
				} else {
					String message = String.format("Document[%s] ingestion not complete yet;", videoDocumentId);
					logger.info(message);
					return message;
				}
			}
			// If status was already 'Completed' in cosmosdb or it has been completed from above code
			// Then call GPT-4 and get review
			if (StringUtils.compare(ingestionState.toLowerCase(), "completed") == 0 || isCompleted) {
				// Call the GPT4 VISION with enhancement for video for review
		  		String prompt = String.format(FIND_VIDEO_ENHANCEMENTS_REVIEW_PROMPT, 
		  										aiOps.getAiEndpoint(), 
		  										aiOps.getAiKey(), 
		  										videoDocumentId, 
		  										fileUrl,
		  										videoDocumentId
		  									);
				String message = aoaiVisionOps.getAOAIVideoCompletion(prompt);
				logger.info(message);
				return message;
			}
			// Status is neither Running nor Completed, nor was it completed via above code
			String message = String.format("Document[%s]: Video ingestion and getting description did not succeed. Need human review", videoDocumentId);
			logger.info(message);
			return message;
		} else {
			return "Video extract not found";
		}
	}
}
