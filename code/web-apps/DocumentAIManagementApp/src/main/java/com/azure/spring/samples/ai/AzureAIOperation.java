package com.azure.spring.samples.ai;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AzureAIOperation {
	private String aiEndpoint;
	private String aiKey;
	private String aiVideoIndexName;
	private String aiVideoAPIVersion;
	
    public static Logger logger = LoggerFactory.getLogger(AzureAIOperation.class);

	public AzureAIOperation(String aiEndpoint, String aiKey, String aiVideoIndexName, String aiVideoAPIVersion) {
		super();
		this.aiEndpoint = aiEndpoint;
		this.aiKey = aiKey;
		this.aiVideoIndexName = aiVideoIndexName;
		this.aiVideoAPIVersion = aiVideoAPIVersion;
	}
	
	public String getVideoIngestionState(String videoDocumentId) {
		String ingestionState = "Unknown";
		try {
			String baseUrl = String.format(
											"%scomputervision/retrieval/indexes/%s/ingestions/%s?api-version=%s", 
											this.aiEndpoint, 
											videoDocumentId,
											videoDocumentId,
											this.aiVideoAPIVersion
										  );
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet aiGetVideoIngestionState = new HttpGet(baseUrl);
			aiGetVideoIngestionState.setHeader("Ocp-Apim-Subscription-Key", this.aiKey);
			
			// Send the request and get the response
			HttpResponse response = httpClient.execute(aiGetVideoIngestionState);
			StatusLine statusOfTheCall = response.getStatusLine();
			
			int statusCode = statusOfTheCall.getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				ingestionState = String.format("GetIngestionState Failed with error: %s and reason: %s", statusCode, statusOfTheCall.getReasonPhrase());
				logger.info(ingestionState);
			} else {
				ingestionState = getVideoIngestionStateFromHttpResponse(response);
				logger.info("Vision API returned: {}", ingestionState);
			}
			return ingestionState;			
		}
		catch (Exception e) {
			ingestionState = String.format("AI Vision API over HTTP raised exception: %s", e);
			logger.info(ingestionState);
			return ingestionState;
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
	
	public int deleteVideoIndex(String videoIndex) {
		String ingestionState = "Unknown";
		try {
			String baseUrl = String.format(
											"%scomputervision/retrieval/indexes/%s?api-version=%s", 
											this.aiEndpoint, 
											videoIndex,
											this.aiVideoAPIVersion
										  );
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpDelete videoIndexDelete = new HttpDelete(baseUrl);
			videoIndexDelete.setHeader("Ocp-Apim-Subscription-Key", this.aiKey);
			
			// Send the request and get the response
			HttpResponse response = httpClient.execute(videoIndexDelete);
			StatusLine statusOfTheCall = response.getStatusLine();
			
			int statusCode = statusOfTheCall.getStatusCode();
			if (statusCode != HttpStatus.SC_NO_CONTENT) {
				ingestionState = String.format("DeleteVideoIndex failed with error: %s and reason: %s", statusCode, statusOfTheCall.getReasonPhrase());
				logger.info(ingestionState);
			} else {
				ingestionState = getVideoIngestionStateFromHttpResponse(response);
				logger.info("Success: Deleted Video Index: {}", videoIndex);
			}
			return statusCode;			
		}
		catch (Exception e) {
			ingestionState = String.format("AI Vision API over HTTP raised exception: %s", e);
			logger.info(ingestionState);
			return HttpStatus.SC_BAD_REQUEST;
		}
	}

	public String getAiEndpoint() {
		return aiEndpoint;
	}

	public void setAiEndpoint(String aiEndpoint) {
		this.aiEndpoint = aiEndpoint;
	}

	public String getAiKey() {
		return aiKey;
	}

	public void setAiKey(String aiKey) {
		this.aiKey = aiKey;
	}

	public String getAiVideoIndexName() {
		return aiVideoIndexName;
	}

	public void setAiVideoIndexName(String aiVideoIndexName) {
		this.aiVideoIndexName = aiVideoIndexName;
	}

	public String getAiVideoAPIVersion() {
		return aiVideoAPIVersion;
	}

	public void setAiVideoAPIVersion(String aiVideoAPIVersion) {
		this.aiVideoAPIVersion = aiVideoAPIVersion;
	}

}
