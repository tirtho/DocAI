package com.azure.spring.samples.anomaly.attachment;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.cosmosdb.CosmosDBCommonQueries;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.utils.FileType;

public class DefaultAttachmentAnomaly implements AttachmentAnomaly {

    public static Logger logger = LoggerFactory.getLogger(DefaultAttachmentAnomaly.class);
    
	private static String FIND_IMAGE_ENHANCEMENTS_PROMPT = 
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
			+ "                \"text\": \"Identify image is a CAR, HOME or OTHER. Detect any damage to the car or home, else say NONE. Also return any text detected in the image. Retur JSON data ONLY in the format:"
			+ "							 { 'imageOf': 'CAR or HOME or OTHER', 'description': <describe image in less than 500 words>, 'anyDamage': <list all damaged, discolored, broken and missing items in the image of the car or home. List NONE if no damaged items found.>, 'surroundings': <any location or surrounding or background information>, 'anyText': <return any text detected in the image>}\" \r\n"
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

	private static String FIND_IMAGE_PROMPT = 
			"{" 
			+ "    \"messages\": [ \r\n"
			+ "        { \"role\": \"system\", \"content\": \"You are a helpful assistant.\" }, \r\n"
			+ "        { \"role\": \"user\", \r\n"
			+ "        \"content\": [  \r\n"
			+ "            { \r\n"
			+ "                \"type\": \"text\", \r\n"
			+ "                \"text\": \"Identify image is a CAR, HOME or OTHER. Detect any damage to the car or home, else say NONE. Also return any text detected in the image. Retur JSON data ONLY in the format:"
			+ "							 { 'imageOf': 'CAR or HOME or OTHER', 'anyDamage': <describe any detected damange to car or home, else just return NONE>, 'anyText': <return any text detected in the image>}\" \r\n"
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
		
    private CosmosDBOperation cosmosDB;
    private AzureOpenAIOperation aoaiOps;
    private AzureOpenAIOperation aoaiVisionOps;
    private String aiEndpoint;
    private String aiKey;
    private String blobStoreSASToken;
	
    /**
     * 
     * @param cosmosDB
     * @param aoaiOps
     * @param aoaiVisionOps
     * @param aiEndpoint
     * @param aiKey
     */
    public DefaultAttachmentAnomaly(
    				CosmosDBOperation cosmosDB,
    				AzureOpenAIOperation aoaiOps,
    				AzureOpenAIOperation aoaiVisionOps, 
    				String aiEndpoint, 
    				String aiKey,
    				String blobStoreSASToken
    			) {
		super();
		this.cosmosDB = cosmosDB;
		this.aoaiOps = aoaiOps;
		this.aoaiVisionOps = aoaiVisionOps;
		this.aiEndpoint = aiEndpoint;
		this.aiKey = aiKey;
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
	  		String prompt = String.format(FIND_IMAGE_ENHANCEMENTS_PROMPT, aiEndpoint, aiKey, fileUrl);
	  		logger.info(prompt);
	  		reviewMessage = String.format("Image Review: %s", aoaiVisionOps.getAOAIVisionCompletion(prompt, true));
	  	} else if (ft.isAudio()) {
	  		// TODO: pass prompt for audio
	  	} else if (ft.isVideo()) {
	  		// TODO: pass prompt for video
	  	} else if (ft.isText()) {
	  		// TODO: pass prompt for text
	  	} else if (ft.isDocument()) {
	  		// TODO: pass prompt for docs like word or pdf etc..
	  	}
	  	
  		logger.info("Default Anomaly returned response: %s", reviewMessage);
  		reviewSummary.add(reviewMessage);
		return reviewSummary;
	}

}
