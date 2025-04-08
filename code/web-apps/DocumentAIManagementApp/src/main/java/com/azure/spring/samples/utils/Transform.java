package com.azure.spring.samples.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.model.AttachmentExtractsData;
import com.azure.spring.samples.model.ExtractData;
import com.google.gson.Gson;

public class Transform {
	
	public static String b64Decode(String dataStr) {
		byte[] decodedBytes = Base64.getDecoder().decode(dataStr);
		String rawToken = new String(decodedBytes, StandardCharsets.UTF_8);
		rawToken = StringUtils.removeStart(rawToken, "['");
		rawToken = StringUtils.removeEnd(rawToken, "']");
		return rawToken;
	}
	
	public static String attachmentExtractsDataToJson(AttachmentExtractsData aed) {
  		List<ExtractData> extracts = aed.getExtracts();
  		// TODO: Filter out the irrelevant data like document Id etc...which are not needed 
  		// when sending to a LLM prompt
        Gson gson = new Gson();
        String jsonString = gson.toJson(extracts);
  		return jsonString;
	}
}
