package com.azure.spring.samples.aoai;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestAssistantMessage;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AzureOpenAIOperation {

    public static Logger logger = LoggerFactory.getLogger(AzureOpenAIOperation.class);
    public static enum AOAIConnectionType {
    	SDK,
    	HTTP
    }

    private static OpenAIServiceVersion AOAI_DEFAULT_VERSION = OpenAIServiceVersion.V2023_07_01_PREVIEW;
    		
    private OpenAIClient aoaiClient;
    private String endpoint;
    private String key;
    private String deployedModel;
    private String modelVersion;
    private String deployedVideoModel;
    private String videoModelVersion;
    private AOAIConnectionType type;

    /**
     * The connectionType is one of the following - 
     * 		'sdk'  - when you want to leverage the Java AOAI sdk package 
     * 		'http' - when you want to connect to AOAI directly via REST API (this is needed for VISION for example)
     * 
     * @param endpoint
     * @param key
     * @param deployedModel
     * @param version
     * @param connectionType
     */
	public AzureOpenAIOperation(
			String endpoint, 
			String key,
			String deployedModel,
			String modelVersion,
			String deployedVideoModel,
			String videoModelVersion,
			AOAIConnectionType connectionType) {
		super();
		this.endpoint = endpoint;
		this.key = key;
		this.deployedModel = deployedModel;
		this.modelVersion = modelVersion;
		this.deployedVideoModel = deployedVideoModel;
		this.videoModelVersion = videoModelVersion;
		this.type = connectionType;
		
		if (type.equals(AOAIConnectionType.SDK)) {
			OpenAIServiceVersion usingAPIVersion = AOAI_DEFAULT_VERSION;
			String transformedVersion = StringUtils.replace(modelVersion, "-", "_");
			transformedVersion = "V" + transformedVersion;
			for (OpenAIServiceVersion aVersion : OpenAIServiceVersion.values()) {
				if (StringUtils.compare(transformedVersion.toLowerCase(), aVersion.getVersion().toLowerCase()) == 0) {
					logger.info("Found matching API version for AOAI Vision API: %s", aVersion.toString());
					usingAPIVersion = aVersion;
					break;
				}
			}
			aoaiClient = new OpenAIClientBuilder()
							    .credential(new AzureKeyCredential(key))
							    .endpoint(endpoint)
								.serviceVersion(usingAPIVersion)
							    .buildClient();
			this.modelVersion = usingAPIVersion.toString();
		} else {
			this.modelVersion = modelVersion;
		}
	}

	public String getAOAIChatCompletion(String prompt) {
		List<ChatRequestMessage> chatMessages = new ArrayList<>();
		chatMessages.add(new ChatRequestSystemMessage("You are an AI assistant that helps people find information."));
		chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
		chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
		chatMessages.add(new ChatRequestUserMessage(prompt));
		ChatCompletionsOptions cco = new ChatCompletionsOptions(chatMessages);
		cco.setTemperature(0.0);
		cco.setFrequencyPenalty(0.0);
		ChatCompletions chatCompletions = aoaiClient.getChatCompletions (this.deployedModel,cco);

		logger.info("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
		StringBuffer completionBuffer = new StringBuffer();
		
		for (ChatChoice choice : chatCompletions.getChoices()) {
		    ChatResponseMessage message = choice.getMessage();
		    logger.info("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
		    logger.info("Message:");
		    logger.info(message.getContent());
		    completionBuffer.append("Model[").append(this.deployedModel).append("]: ").append(message.getContent());
		}
		logger.info(completionBuffer.toString());
		return completionBuffer.toString();
	}
	
	public String getAOAIOmniCompletion(String prompt, boolean withExtensions) {
		try {
			String extensionUri = "";
			if (withExtensions) {
				extensionUri = "extensions/";
			}
			String baseUrl = String.format(
											"%sopenai/deployments/%s/%schat/completions?api-version=%s", 
											this.endpoint, 
											this.deployedModel,
											extensionUri,
											this.modelVersion
										  );
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost aoaiOmniPost = new HttpPost(baseUrl);
			aoaiOmniPost.setHeader("Content-Type", "application/json");
			aoaiOmniPost.setHeader("api-key", this.key);
			logger.info("Prompt sent to GPT-4 Omni API: {}", prompt);
			StringEntity entity = new StringEntity(prompt);
			aoaiOmniPost.setEntity(entity);
			
			String promptCompletion;
			// Send the request and get the response
			HttpResponse response = httpClient.execute(aoaiOmniPost);
			
			StatusLine statusOfTheCall = response.getStatusLine();
			int statusCode = statusOfTheCall.getStatusCode();
			if ( statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED) {
				promptCompletion = String.format("GPT-4 Omni API call failed with error code: %s and reason: %s", statusCode, statusOfTheCall.getReasonPhrase());
				logger.info(promptCompletion);
			} else {
				promptCompletion = getCompletionFromHttpResponse(response);
				logger.info("GPT-4 Omni API returned: {}", promptCompletion);
			}
			
			return promptCompletion;			
		}
		catch (Exception e) {
			String promptFailedMessage = String.format("Connecting to AOAI GPT-4 Omni API over HTTP raised exception: %s", e);
			logger.info(promptFailedMessage);
			return promptFailedMessage;
		}
	}
	
	/**
	 * This makes a call to the Vision API with enhancements. 
	 * And this is using direct http connection
	 * @param prompt
	 * @return
	 */
	public String getAOAIVisionCompletion(String prompt, boolean withExtensions) {
		try {
			String extensionUri = "";
			if (withExtensions) {
				extensionUri = "extensions/";
			}
			String baseUrl = String.format(
											"%sopenai/deployments/%s/%schat/completions?api-version=%s", 
											this.endpoint, 
											this.deployedVideoModel,
											extensionUri,
											this.videoModelVersion
										  );
			HttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost aoaiVisionPost = new HttpPost(baseUrl);
			aoaiVisionPost.setHeader("Content-Type", "application/json");
			aoaiVisionPost.setHeader("api-key", this.key);
			logger.info("Prompt sent to GPT-4 Turbo Vision API: {}", prompt);
			StringEntity entity = new StringEntity(prompt);
			aoaiVisionPost.setEntity(entity);
			
			String promptCompletion;
			// Send the request and get the response
			HttpResponse response = httpClient.execute(aoaiVisionPost);
			
			StatusLine statusOfTheCall = response.getStatusLine();
			int statusCode = statusOfTheCall.getStatusCode();
			if ( statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED) {
				promptCompletion = String.format("Vision API call failed with error code: %s and reason: %s", statusCode, statusOfTheCall.getReasonPhrase());
				logger.info(promptCompletion);
			} else {
				promptCompletion = getCompletionFromHttpResponse(response);
				logger.info("Vision API returned: {}", promptCompletion);
			}
			
			return promptCompletion;			
		}
		catch (Exception e) {
			String promptFailedMessage = String.format("Connecting to AOAI Vision API over HTTP raised exception: %s", e);
			logger.info(promptFailedMessage);
			return promptFailedMessage;
		}
	}
	
	public String getAOAIVideoCompletion(String prompt) {
		return getAOAIVisionCompletion(prompt, true);
	}
	
	private String getCompletionFromHttpResponse(HttpResponse response) {
		// Get the JSON response body as a string
		String promptCompletion;
		try {
			String responseBody = EntityUtils.toString(response.getEntity());
			// Parse the JSON string
			JsonElement jsonElement = JsonParser.parseString(responseBody);
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			String model = jsonObject.get("model").getAsString();
			JsonElement theChoicesElements = jsonObject.get("choices");
			JsonArray theChoicesArray = theChoicesElements.getAsJsonArray();
			StringBuffer completionBuffer = new StringBuffer();
			for (JsonElement aChoice : theChoicesArray) {
				JsonObject aChoiceInJson = aChoice.getAsJsonObject();
				JsonElement aMessageElements = aChoiceInJson.get("message");
				JsonObject aMessageInJson = aMessageElements.getAsJsonObject();			
				JsonElement theContentElement = aMessageInJson.get("content");
				String aCompletion = theContentElement.getAsString();
				aCompletion = StringUtils.removeStart(aCompletion, "```json");
				aCompletion = StringUtils.removeEnd(aCompletion, "```");
				aCompletion = StringUtils.trim(aCompletion);
				completionBuffer.append(aCompletion).append(", ");
			}
			// Remove the , from the last in the list of json elements
			String aggregatedCompletion = StringUtils.removeEnd(completionBuffer.toString(), ", ");
			promptCompletion = String.format("Model[%s]: %s", model, StringUtils.trim(aggregatedCompletion));
			logger.info("Prompt Completion returned {}", promptCompletion);
			return promptCompletion;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			promptCompletion = String.format("Prompt Completion failed with exception: {}", e);
			logger.info(promptCompletion);
			return promptCompletion;
		}
	}
}
