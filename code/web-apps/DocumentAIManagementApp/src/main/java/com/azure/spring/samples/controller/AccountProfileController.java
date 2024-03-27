// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.samples.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.azure.spring.samples.DocumentAIManagementAppAuthorization;
import com.azure.spring.samples.model.LoggedInUserProfile;
import com.azure.spring.samples.utils.ReturnEntity;

@RestController
public class AccountProfileController {

    private static Logger logger = LoggerFactory.getLogger(AccountProfileController.class);
       
    // Demo users who are allowed to use this App
    @Value("${docai.approved.demo.users}")
    private String demoUsers;
        
    public AccountProfileController() {
    }

    @RequestMapping("/home")
    public Map<String, Object> home() {
        logger.info("Request '/home' path.");
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("content", "home");
        return model;
    }
    
    /**
     * HTTP GET SCORE
     */
    @RequestMapping(value = "/api/getAccountProfile/", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
	@PreAuthorize("hasAuthority('APPROLE_DocAIDemo-DemoUser')")
    public ResponseEntity<?> getAccountProfile(
    		OAuth2AuthenticationToken authToken
    	) {
		String responseMessage;
    	ReturnEntity<Boolean, LoggedInUserProfile> callingUser = DocumentAIManagementAppAuthorization.authorizeUser(authToken, demoUsers);
        if (callingUser.getStatus() == false || callingUser.getEntity() == null) {
        	responseMessage = "AUTHORIZATION FAILED: GET request access for user: " + callingUser.getEntity().getPreferredUserName();
        	logger.info("{}", responseMessage);
        	return new ResponseEntity<>(responseMessage, HttpStatus.METHOD_NOT_ALLOWED);
        }
        logger.info("GET request access '/api/getAccountProfile' path.");

        return new ResponseEntity<>(callingUser.getEntity(), HttpStatus.OK);
    }
    
}
