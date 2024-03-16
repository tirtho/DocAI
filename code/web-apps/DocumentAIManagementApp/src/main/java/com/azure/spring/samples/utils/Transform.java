package com.azure.spring.samples.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

public class Transform {
	
	public static String b64Decode(String dataStr) {
		byte[] decodedBytes = Base64.getDecoder().decode(dataStr);
		String rawToken = new String(decodedBytes, StandardCharsets.UTF_8);
		rawToken = StringUtils.removeStart(rawToken, "['");
		rawToken = StringUtils.removeEnd(rawToken, "']");
		return rawToken;
	}
}
