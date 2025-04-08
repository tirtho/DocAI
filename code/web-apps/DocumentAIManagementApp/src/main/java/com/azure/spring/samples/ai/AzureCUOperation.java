package com.azure.spring.samples.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.utils.ReturnEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AzureCUOperation {
	private String cuEndpoint;
	private String cuKey;
	private String cuAnalyzerId;
	private String cuAPIVersion;
	
    public static Logger logger = LoggerFactory.getLogger(AzureCUOperation.class);
	
	public AzureCUOperation(String cuEndpoint, String cuKey, String cuAnalyzerId, String cuAPIVersion) {
		super();
		this.cuEndpoint = cuEndpoint;
		this.cuKey = cuKey;
		this.cuAnalyzerId = cuAnalyzerId;
		this.cuAPIVersion = cuAPIVersion;
	}
	
	@SuppressWarnings("unchecked")
	// Returns ReturnEntity<String, List<Map<String, ?>>>
	// where first arg is the ingestion status and the second one is the list of fields
	public <U, V> ReturnEntity<U, V> getVideoExtractionResult(String fileUrl, String operationId) {

		String message = "Error: Unknown";
		String status = "Failed";
		List<Map<String, ?>> updatedFields = null;
		// Run the CU GetResult REST API call to get the extracted data
		if (operationId != null) {
			message = String.format("Operation id[%s] : Calling CU GetResult", operationId);
			logger.info(message);
			ReturnEntity<String, String> responseEntity = cuGetResult(operationId);
			
			if (responseEntity != null && responseEntity.getEntity() != null) {
				try {
					message = String.format("Success: Operation Id[%s] CU GetResult returned;",
							operationId);
					logger.info(message);
					// Parse the JSON string
					JsonElement jsonElement = JsonParser.parseString(responseEntity.getEntity());
					JsonObject responseJson = jsonElement.getAsJsonObject();
					status = responseJson.get("status").getAsString();
					
					// Now update the status and description fields in cosmosdb
					updatedFields = new ArrayList<>();
										
					JsonObject resultJson = responseJson.get("result").getAsJsonObject();
					JsonArray contentsArrayJson = resultJson.getAsJsonArray("contents");
					int shotIndex = 0;
					String keyNamingFormat = "Shot[%d]-%s";
					for (JsonElement contentElementJson : contentsArrayJson) {
						JsonObject contentJson = contentElementJson.getAsJsonObject();
						Map<String, JsonElement> contentElementsMap = contentJson.asMap();
						for (String contentElementName : contentElementsMap.keySet()) {
							if (StringUtils.compare(contentElementName, "fields") == 0) {
								// Add fields at field level... these are in the schema 
								// defined by the CU analyzer schema
								JsonObject fieldsJson = contentJson.get("fields").getAsJsonObject();
								Map<String, JsonElement> fieldElementsMap = fieldsJson.asMap();
								for (String fieldName : fieldElementsMap.keySet()) {
									JsonObject fieldObject = fieldElementsMap.get(fieldName).getAsJsonObject();
									Map cuField = new HashMap<>();
									String indexedFieldName = String.format(keyNamingFormat, shotIndex, fieldName);
									cuField.put("fieldName", indexedFieldName);
									cuField.put("fieldValueType", fieldObject.get("type").getAsString());
									cuField.put("fieldValue", fieldObject.get("valueString").getAsString());
									updatedFields.add((HashMap<String, ?>) cuField);
								}
							} else {
								//JsonObject contentObject = contentElementsMap.get(contentElementName).getAsJsonObject();
								// Add the fields at the content level, like mark down...
								Map aField = new HashMap<>();
								aField.put("fieldName", String.format(keyNamingFormat, shotIndex, contentElementName));
								aField.put("fieldValueType", "string");
								aField.put("fieldValue", contentJson.get(contentElementName).getAsString());
								updatedFields.add((HashMap<String, ?>) aField);						
							}
						}
						shotIndex++;
					}
					message = String.format("Success: Operation Id[%s] CU GetResult returned content has been parsed;",
							operationId);
					logger.info(message);
				} catch (Exception e) {
					message = String.format("Error: Content Understanding response json parsing exception: %s", e);
					logger.info(message);
					status = "ParsingError";
				}
			} else {
				message = String.format("Error: Content Understanding response returned nulls");
				logger.info(message);
				status = "CUReturnedNull";
			}
		} else {
			message = String.format("No CU GetResult call made as operation id is null");
			logger.info(message);
			status = "OperationIdNull";
		}
		return (ReturnEntity<U, V>) new ReturnEntity<String, List<Map<String, ?>>>(status, updatedFields);
	}
	
	@SuppressWarnings("unchecked")
	public <U, V> ReturnEntity<U, V> cuGetResult(String operationId) {
		String message = null;
		try {
			String baseUrl = String.format(
											"%scontentunderstanding/analyzers/%s/results/%s?api-version=%s", 
											this.cuEndpoint, 
											this.cuAnalyzerId,
											operationId,
											this.cuAPIVersion
										  );
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet cuGetResult = new HttpGet(baseUrl);
			cuGetResult.setHeader("Ocp-Apim-Subscription-Key", this.cuKey);
			
			// Send the request and get the response
			HttpResponse response = httpClient.execute(cuGetResult);
			StatusLine statusOfTheCall = response.getStatusLine();
			
			int statusCode = statusOfTheCall.getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				message = String.format("Error: Content Understanding GetResult error: %s and reason: %s", statusCode, statusOfTheCall.getReasonPhrase());
				logger.info(message);
				return (ReturnEntity<U, V>) new ReturnEntity<String, String>(message, null);
			} else {
				try {
					String responseBody = EntityUtils.toString(response.getEntity());
					message = String.format("Success: Content Understanding GetResult succeeded");
					return (ReturnEntity<U, V>) new ReturnEntity<String, String>(message, responseBody);
				} catch (Exception e) {
					message = String.format("Error: Content Understanding response parsing failed with exception: %s", e);
					logger.info(message);
					return (ReturnEntity<U, V>) new ReturnEntity<String, String>(message, null);
				}
			}
		}
		catch (Exception e) {
			message = String.format("Error: Content Understanding GetResult REST Api over HTTP raised exception: %s", e);
			logger.info(message);
			return (ReturnEntity<U, V>) new ReturnEntity<String, String>(message, null);
		}
	}
	
	public String getVideoIngestionStateFromHttpResponse(HttpResponse response) {
			// Get the JSON response body as a string
			String ingestionState = "Unknown";
			try {
				String responseBody = EntityUtils.toString(response.getEntity());
				// Parse the JSON string
				JsonElement jsonElement = JsonParser.parseString(responseBody);
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				ingestionState = jsonObject.get("state").getAsString();
				return ingestionState;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				ingestionState = String.format("GetVideoIngestion response parsing failed with exception: %s", e);
				logger.info(ingestionState);
				return ingestionState;
			}
	}

	public int deleteOperation(String operationId) {
		// TODO If we need to delete the CU operation result 
		// where CU allows it as some point in future
		return HttpStatus.SC_NO_CONTENT;
	}

}

/**
------------------------------	
	@SuppressWarnings("unchecked")
	private <U, V> ReturnEntity<U, V> processVideoExtraction(
			String fileUrl,
			AttachmentExtractsData aed, 
			ExtractData extract, 
			Map<String, ?> videoDocumentIdField,
			Map<String, ?> ingestionStatusField
		) {
		
		String message = null;
		String videoDocumentId = (String) videoDocumentIdField.get("fieldValue");
		String ingestionState = (String) ingestionStatusField.get("fieldValue");
		if (videoDocumentId != null && ingestionState != null) {
			// Time to make another getIngestionState() call to update status
			// Get Description also again or for the first time
			
			HERE write code to implement the Get Results REST API in CU
			// Check if the ingestion status from the extract data read from cosmos db
			// says the ingestion is still in "Running" state
			// If state is "Succeeded", there is nothing to do
			// Else run the CU Get Result REST API call to get the extracted data
			if (StringUtils.compare(ingestionState.toLowerCase(), "running") == 0) {
				cuOps.getCUResult()
			} else {
				message = String.format("Success: Extraction of Video[%s] already completed, no CU operation needed;", videoDocumentId);
				logger.info(message);				
			}
			
			
			
			String state = cuOps.getVideoIngestionState(videoDocumentId);
			if (StringUtils.compare(state.toLowerCase(), "completed") == 0) {
				// Use GPT4 Vision to get a Description of the video
				String prompt = String.format(FIND_VIDEO_ENHANCEMENTS_DESCRIPTION_PROMPT, aiOps.getAiEndpoint(),
						aiOps.getAiKey(), videoDocumentId, fileUrl, videoDocumentId);
				// Call GPT4 Vision to get description of the video
				String description = aoaiOps.getAOAIVideoCompletion(prompt);
				
				// Now update the status and description fields in cosmosdb
				List<Map<String, ?>> updatedFields = new ArrayList<>();
				Map descriptionField = new HashMap<>();
				descriptionField.put("fieldName", "Description");
				descriptionField.put("fieldValueType", "string");
				// Open AI does not support logprobs for Vision APIs yet
				// Hence the confidence score is commented out and not computed
				//descriptionField.put("fieldConfidence", 0.99);
				descriptionField.put("fieldValue", description);
				updatedFields.add((HashMap<String, ?>) descriptionField);

				Map statusField = new HashMap<>();
				statusField.put("fieldName", "IngestionStatus");
				statusField.put("fieldValueType", "string");
				statusField.put("fieldConfidence", 0.99);
				statusField.put("fieldValue", "Completed");
				updatedFields.add((HashMap<String, ?>) statusField);

				ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
				String formattedTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
				Map lastUpdatedTimeField = new HashMap<>();
				lastUpdatedTimeField.put("fieldName", "LastUpdatedTime");
				lastUpdatedTimeField.put("fieldValueType", "string");
				lastUpdatedTimeField.put("fieldConfidence", 0.99);
				lastUpdatedTimeField.put("fieldValue", formattedTime);
				updatedFields.add((HashMap<String, ?>) lastUpdatedTimeField);

				extract.upsertFields(updatedFields);
				// Update the 'IngestionStatus' and 'Description' fields for this attachment in
				// cosmosdb
				CosmosDBCommonQueries.upsertAttachmentExtractedData(aed, cosmosDB);

				message = String.format("Success: Document[%s] status and description fields updated;",
						videoDocumentId);
				logger.info(message);
			} else {
				message = String.format("Success: Document[%s] ingestion not complete yet;", videoDocumentId);
				logger.info(message);
			}
		} else {
			message = "Error: Video extract not found";
		}
		
		return (ReturnEntity<U, V>) new ReturnEntity<String, ExtractData>(message, extract);

	}
*/