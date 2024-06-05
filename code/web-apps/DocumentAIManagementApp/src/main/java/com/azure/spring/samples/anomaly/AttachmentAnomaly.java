package com.azure.spring.samples.anomaly;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.model.ExtractData;
import com.google.gson.Gson;

public interface AttachmentAnomaly {
    
	List<?> getAttachmentAnomaly (String attachmentId, String attachmentCategory);
	
	default String attachmentExtractsDataRquiredFieldsReview (AttachmentExtractsData aed, AzureOpenAIOperation aoaiOps, String prompt) {
  	  	// Make the AOAI call and let it find missing required fields		
  	  	Gson gson = new Gson();
  	  		
  		String jsonAttachmentData = gson.toJson(aed);
  		String quoteCleanedJson = StringUtils.replace(jsonAttachmentData, "\"", "'");
  		StringBuffer promptStringBuffer = new StringBuffer();
  		String promptStr = promptStringBuffer.append(prompt).append(quoteCleanedJson).toString();
  		String aoaiReview = aoaiOps.getAOAIChatCompletion(promptStr);
		return aoaiReview;
	}
	
	default Map<String, String> searchFieldValueMapFromAttachmentExtractsData(AttachmentExtractsData aed, String searchFieldsString) {
		List<String> fieldNameArray = Arrays.asList(StringUtils.split(searchFieldsString, ", "));
		
		Map<String, String> resultantFields = new HashMap<>();
		for (ExtractData ed : aed.getExtracts()) {
			for (Map<String, ?> field : ed.getFields()) {
				String fieldName = (String) field.get("fieldName");
				if (fieldNameArray.contains(fieldName)) {
					String fieldValue = String.format("%s", field.get("fieldValue"));
					resultantFields.put(fieldName, fieldValue);
				}
			}
		}
		return resultantFields;
	}
}
