package com.azure.spring.samples.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
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
		// No secret from environment variables
		// Try Managed Identity
		if (StringUtils.isBlank(cuKey)) {
	        String scope = "https://cognitiveservices.azure.com/.default";
	        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
	        try {
	            // Get the access token
	            AccessToken accessToken = credential.getToken(
									            		new TokenRequestContext()
									            		.addScopes(scope)
									            	)
									            	.block();
	            // Extract the token string
	            this.cuKey = accessToken.getToken();
	            // OffsetDateTime expiry = accessToken.getExpiresAt();
	        } catch (Exception e) {
	            logger.info("Exception raised trying to get token using MI for endpoint {} :: {}", cuEndpoint, e );
	        }
		}
	}
	
	@SuppressWarnings("unchecked")
	// Returns ReturnEntity<String, List<Map<String, ?>>>
	// where first arg is the ingestion status and the second one is the list of fields
	public <U, V> ReturnEntity<U, V> getVideoExtractionResult(String operationId) {

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
					
					// Now update the status and other fields
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
			// cuGetResult.setHeader("Ocp-Apim-Subscription-Key", this.cuKey);
			cuGetResult.setHeader("Authorization", "Bearer " + this.cuKey);
			
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
				ingestionState = String.format("GetVideoIngestion response parsing failed with exception: %s", e);
				logger.info(ingestionState);
				return ingestionState;
			}
	}

	public int deleteOperation(String operationId) {
		// TODO If we need to delete the CU operation result 
		// where CU allows it as some point in future
		// Right now CU doesn't have an API to delete
		return HttpStatus.SC_NO_CONTENT;
	}

}
