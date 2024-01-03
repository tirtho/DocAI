package com.azure.spring.samples.aoai;

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

    private OpenAIClient aoaiClient;
    private String deployedModel;

	public AzureOpenAIOperation(String endpoint, String key, String deployedModel) {
		super();
		aoaiClient = new OpenAIClientBuilder()
								    .credential(new AzureKeyCredential(key))
								    .endpoint(endpoint)
									.serviceVersion(OpenAIServiceVersion.V2023_07_01_PREVIEW)
								    .buildClient();
		this.deployedModel = deployedModel;
	}

	public String getAOAIChatCompletion(String prompt) {
		List<ChatRequestMessage> chatMessages = new ArrayList<>();
		chatMessages.add(new ChatRequestSystemMessage("You are an AI assistant that helps people find information."));
		chatMessages.add(new ChatRequestUserMessage("Can you help me?"));
		chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
		chatMessages.add(new ChatRequestUserMessage(prompt));
		ChatCompletions chatCompletions = aoaiClient.getChatCompletions (deployedModel,
															new ChatCompletionsOptions(chatMessages)
														);

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
	
	
	
}
