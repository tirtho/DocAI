// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.samples.controller;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.azure.ai.textanalytics.TextAnalyticsClient;
import com.azure.ai.textanalytics.TextAnalyticsClientBuilder;
import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.samples.DocumentAIManagementAppAuthorization;
import com.azure.spring.samples.model.BingWebPages;
import com.azure.spring.samples.model.LoggedInUserProfile;
import com.azure.spring.samples.model.SearchItem;
import com.azure.spring.samples.model.SearchResult;
import com.azure.spring.samples.utils.ReturnEntity;

@RestController
public class SearchAndReviewController {

    private static Logger logger = LoggerFactory.getLogger(SearchAndReviewController.class);
   
    @Value("${azure.bing.api.key}")
    private String azureBingKey;
    @Value("${azure.bing.query.count}")
    private Integer azureBingQueryCount;

    @Value("${azure.bing.ai.api.key}")
    private String azureBingCognitiveServiceKey;
    @Value("${azure.bing.ai.api.endpoint}")
    private String azureBingCognitiveServiceEndpoint;
    
    // Demo users who are allowed to use this App
    @Value("${docai.approved.demo.users}")
    private String demoUsers;

    
    static String bingSearchHost = "https://api.bing.microsoft.com";
    static String bingSearchPath = "/v7.0/search";
    
    public SearchAndReviewController() {
    }

//    @RequestMapping("/home")
//    public Map<String, Object> home() {
//        logger.info("Request '/home' path.");
//        final Map<String, Object> model = new HashMap<String, Object>();
//        model.put("content", "home");
//        return model;
//    }
    
    /**
     * HTTP GET SCORE
     */
    @RequestMapping(value = "/api/searchAndReview/{keyPhrases}", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@PreAuthorize("hasAuthority('APPROLE_DocAIDemo-DemoUser')")
    public ResponseEntity<?> getInsurnaceScore(
    		OAuth2AuthenticationToken authToken,
    		@PathVariable String keyPhrases
    	) {
		String responseMessage;
    	ReturnEntity<Boolean, LoggedInUserProfile> callingUser = DocumentAIManagementAppAuthorization.authorizeUser(authToken, demoUsers);
        if (callingUser.getStatus() == false || callingUser.getEntity() == null) {
        	responseMessage = "AUTHORIZATION FAILED: GET request access for user: " + callingUser.getEntity().getPreferredUserName();
        	logger.info("{}", responseMessage);
        	return new ResponseEntity<>(responseMessage, HttpStatus.METHOD_NOT_ALLOWED);
        }
        logger.info("GET request access '/api/searchAndReview' path with item: {}", keyPhrases);
        String score = Double.toString(Math.random());

        return new ResponseEntity<>(score, HttpStatus.OK);
    }
    
    /**
     * HTTP POST NEW ONE
     */
    @RequestMapping(value = "/api/searchAndReview/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('APPROLE_DocAIDemo-DemoUser')")
    public ResponseEntity<?> searchOnlineForInsurability(
    		OAuth2AuthenticationToken authToken,
    		@RequestBody SearchItem searchItem
    	) {
		String responseMessage;
    	ReturnEntity<Boolean, LoggedInUserProfile> callingUser = DocumentAIManagementAppAuthorization.authorizeUser(authToken, demoUsers);
        if (callingUser.getStatus() == false || callingUser.getEntity() == null) {
        	responseMessage = "AUTHORIZATION FAILED: POST request access for user: " + callingUser.getEntity().getPreferredUserName();
        	logger.info("{}", responseMessage);
        	return new ResponseEntity<>(responseMessage, HttpStatus.METHOD_NOT_ALLOWED);
        }
        logger.info("POST request access '/api/searchAndReview' path with item: {}", searchItem);
        try {
        	BingWebPages webPages = searchWeb(searchItem.getDescription());

        	if (webPages != null && !webPages.getJsonResponse().isEmpty()) {
        		List<SearchResult> searchResults = webPages.getSearchResults();
	        	if (searchResults != null && !searchResults.isEmpty()) {
	        		// add the key phrases
	        		for (SearchResult sr : searchResults) {
	        			List<String> kphrases = getKeyPhrases(sr.getSearchResultString());
	        			sr.setKeyPhrases(kphrases);
	        		}
	        		return new ResponseEntity<>(searchResults, HttpStatus.OK);
	        	}
        	}       	
        	logger.info("Found Nothing from searching for {}", searchItem.getDescription());
            return new ResponseEntity<>("Nothing found", HttpStatus.NOT_FOUND);            
        } catch (Exception e) {
        	logger.info("Bing Search failed: {}", e);
        	return new ResponseEntity<>("Failed Search", HttpStatus.NOT_FOUND);
        }
    }
    
    public BingWebPages searchWeb (String searchQuery) throws Exception {
        // Construct the URL.
        URL url = new URL(bingSearchHost + bingSearchPath + "?count=" + azureBingQueryCount + "&q=" +  URLEncoder.encode(searchQuery, "UTF-8"));

        // Open the connection.
        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        
		if (StringUtils.isBlank(azureBingKey)) {
			logger.info("Bing Search API Key is blank, so attempting to get token using Managed Identity");
			String token = getToken(azureBingKey);
	        //connection.setRequestProperty("Authorization", "Bearer " + token);
	        connection.setRequestProperty("Ocp-Apim-Subscription-Key", token);
		} else {
			logger.info("Bing Search API key is NOT blank, so using it to access Bing Search Service");
	        connection.setRequestProperty("Ocp-Apim-Subscription-Key", azureBingKey);
		}
        
        // Receive the JSON response body.
        InputStream stream = connection.getInputStream();
        String response = new Scanner(stream).useDelimiter("\\A").next();

        // Construct the result object.
        BingWebPages results = new BingWebPages(new HashMap<String, String>(), response);

        // Extract Bing-related HTTP headers.
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String header : headers.keySet()) {
            if (header == null) continue;      // may have null key
            if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")){
                results.getRelevantHeaders().put(header, headers.get(header).get(0));
            }
        }
        stream.close();
        return results;
    }
    
    public List<String> getKeyPhrases(String textDocument) {
    	
    	
    	TextAnalyticsClientBuilder tacb = new TextAnalyticsClientBuilder();
		if (StringUtils.isBlank(azureBingCognitiveServiceKey)) {
			// Go for Managed Identity
			TokenCredential credential = new DefaultAzureCredentialBuilder().build();
			tacb.credential(credential);
		} else {
			// Go for API Key
	    	AzureKeyCredential credential = new AzureKeyCredential(azureBingCognitiveServiceKey);
			tacb.credential(credential);
		}
    	TextAnalyticsClient textAnalyticsClient = tacb
    												.endpoint(azureBingCognitiveServiceEndpoint)
    												.buildClient();
    	List<String> keyPhrases = new ArrayList<>();
    	textAnalyticsClient.extractKeyPhrases(textDocument).forEach(keyPhrase -> keyPhrases.add(keyPhrase));
    	return keyPhrases;
    }
    
    public String getToken(String key) {
		// No secret from environment variables
		// Try Managed Identity
		if (StringUtils.isBlank(key)) {
	        String scope1 = "https://cognitiveservices.azure.com/.default";
	        String scope2 = "https://search.azure.com/.default";
	        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
	        try {
	            // Get the access token
	            AccessToken accessToken = credential.getToken(
									            		new TokenRequestContext()
									            		.addScopes(scope1)
									            		.addScopes(scope2)
									            	)
									            	.block();
	            logger.info("Received token using Managed Identity to connect to Bing Search");
	            // Extract the token string
	            return accessToken.getToken();
	            // OffsetDateTime expiry = accessToken.getExpiresAt();
	        } catch (Exception e) {
	            logger.info("Exception raised trying to get token using MI for Bing Search :: {}", e );
	        }
		}
        logger.info("Bin Search API key is not blank, so returning the API key");
		return key;
    }
}
