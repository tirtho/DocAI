package com.azure.spring.samples.aoai;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

public class AzureOpenAIOperation {

    public static Logger logger = LoggerFactory.getLogger(AzureOpenAIOperation.class);
    public static enum AOAIConnectionType {
    	SDK,
    	HTTP
    }

    private OpenAIClient aoaiClient;
    private String deployedModel;
    private String endpoint;
    private String key;
    private AOAIConnectionType type;

    /**
     * The connectionType is one of the following - 
     * 		'sdk'  - when you want to leverage the Java AOAI sdk package 
     * 		'http' - when you want to connect to AOAI directly via REST API (this is needed for VISION for example)
     * @param endpoint
     * @param key
     * @param deployedModel
     * @param connectionType
     */
	public AzureOpenAIOperation(String endpoint, String key, String deployedModel, AOAIConnectionType connectionType) {
		super();
		this.type = connectionType;
		this.deployedModel = deployedModel;
		this.endpoint = endpoint;
		this.key = key;
		
		if (type.equals(AOAIConnectionType.SDK)) {
			aoaiClient = new OpenAIClientBuilder()
				    .credential(new AzureKeyCredential(key))
				    .endpoint(endpoint)
					.serviceVersion(OpenAIServiceVersion.V2023_07_01_PREVIEW)
				    .buildClient();
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
		ChatCompletions chatCompletions = aoaiClient.getChatCompletions (deployedModel,cco);

		logger.info("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
		StringBuffer completionBuffer = new StringBuffer();
		
		for (ChatChoice choice : chatCompletions.getChoices()) {
		    ChatResponseMessage message = choice.getMessage();
		    logger.info("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
		    logger.info("Message:");
		    logger.info(message.getContent());
		    completionBuffer.append("Model[").append(deployedModel).append("] : ").append(message.getContent());
		}
		logger.info(completionBuffer.toString());
		return completionBuffer.toString();
	}
	
	/**
	 * This makes a call to the Vision API with enhancements. 
	 * And this is using direct http connection
	 * @param prompt
	 * @return
	 */
	public String getAOAIVisionCompletion(String prompt) {
		URL url;
		try {
			url = new URL(endpoint);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			logger.info("Connecting to AOAI over HTTP for AOAI raised exception: %s", e);
			return null;
		} catch (IOException e) {
			logger.info("Connecting to AOAI over HTTP for AOAI raised exception: %s", e);
			return null;
		}
		return null;
	}
	
	
}
