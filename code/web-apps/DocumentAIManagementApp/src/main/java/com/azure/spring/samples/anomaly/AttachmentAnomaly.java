package com.azure.spring.samples.anomaly;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.google.gson.Gson;

public interface AttachmentAnomaly {
    
	String getAttachmentAnomaly (String attachmentId);
	
	static String attachmentExtractsDataRquiredFieldsReview (AttachmentExtractsData aed, AzureOpenAIOperation aoaiOps, String prompt) {
  	  	// Make the AOAI call and let it find missing required fields		
  	  	Gson gson = new Gson();
  	  		
  		String jsonAttachmentData = gson.toJson(aed);
  		String quoteCleanedJson = StringUtils.replace(jsonAttachmentData, "\"", "'");
  		StringBuffer promptStringBuffer = new StringBuffer();
  		String promptStr = promptStringBuffer.append(prompt).append(quoteCleanedJson).toString();
  		String aoaiReview = aoaiOps.getAOAIChatCompletion(promptStr);
  		String aedId = aed.getId();
		String reviewMessage = String.format("Note: %s[%s] required field(s) check : %s", aed.getAttachmentName(), aedId, aoaiReview);
		return reviewMessage;
	}
}
