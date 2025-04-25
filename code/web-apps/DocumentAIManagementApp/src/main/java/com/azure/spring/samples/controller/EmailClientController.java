// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.samples.controller;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.DocumentAIManagementAppAuthorization;
import com.azure.spring.samples.model.EmailClientItem;
import com.azure.spring.samples.model.LoggedInUserProfile;
import com.azure.spring.samples.utils.ReturnEntity;

import core.azure.spring.samples.messaging.SendOutlookEmail;

@RestController
public class EmailClientController {

    private static Logger logger = LoggerFactory.getLogger(EmailClientController.class);
       
    @Value("${docai.receiver.email.address}")
    private String docAIEmailReceiver;
    @Value("${docai.sender.email.address}")
    private String docAIEmailSender;
    @Value("${docai.logic.app.trigger.subject.prefix}")
    private String docAISubjectPrefix;
    @Value("${docai.approved.demo.users}")
    private String demoUsers;
    
    @Value("${office365.graph.api.aad.client-id}")
	private String clientId;
    @Value("${office365.graph.api.aad.client-secret}")
	private String clientSecret;
    @Value("${office365.graph.api.aad.tenant-id}")
	private String tenantId;	

    private String LOCAL_ATTACHMENTS_FOLDER = "static\\documents\\";
    
    public EmailClientController() {
    }
     
    /**
     * HTTP POST NEW ONE
     */
    @RequestMapping(value = "/api/emailClient/", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasAuthority('APPROLE_DocAIDemo-DemoUser')")
    public ResponseEntity<?> emailClient(
    		OAuth2AuthenticationToken authToken,
    		@RequestBody EmailClientItem emailClientItem
    	) {
    	
		String responseMessage;
    	ReturnEntity<Boolean, LoggedInUserProfile> callingUser = DocumentAIManagementAppAuthorization.authorizeUser(authToken, demoUsers);
        if (callingUser.getStatus() == false || callingUser.getEntity() == null) {
        	responseMessage = "AUTHORIZATION FAILED: POST request access for user: " + callingUser.getEntity().getPreferredUserName();
        	logger.info("{}", responseMessage);
        	return new ResponseEntity<>(responseMessage, HttpStatus.METHOD_NOT_ALLOWED);
        }
        
    	logger.info("POST request access '/api/emailClient/' path by {} with item: {}", callingUser, emailClientItem);

        try {
        	List<String> filesToAttach = addLocaFilePaths(emailClientItem.getAttachments());
        	List<String> emailReceivers = new ArrayList<>();
        	String subject = emailClientItem.getSubject();
        	if (subject == null) {
        		subject = docAISubjectPrefix;
        	} else {
        		subject = docAISubjectPrefix + " " + subject;
        	}
        	emailReceivers.add(docAIEmailReceiver);
        	SendOutlookEmail soe = new SendOutlookEmail(clientId, clientSecret, tenantId);
        	soe.sendMailWithAttachments(
        						docAIEmailSender,
        						emailReceivers,
        						subject, 
        						emailClientItem.getBody(),
        						LOCAL_ATTACHMENTS_FOLDER,
        						filesToAttach
        					);
        	responseMessage = String.format("Email Sent");
        	logger.info("{}", responseMessage);        	
        	return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception e) {
        	responseMessage = String.format("Failed to send email. Raised exception: %s", e);
        	logger.info("{}", responseMessage);
        	return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        }
    }
    
    public List<String> addLocaFilePaths(List<String> attachments) {
    	List<String> fullFilePaths = new ArrayList<>();
    	for (String attachment : attachments) {
    		String[] fileInfos = StringUtils.split(attachment, ':');
    		if (fileInfos.length == 2 && StringUtils.compare(fileInfos[1], "true") == 0) {
        		attachment = fileInfos[0];
        		fullFilePaths.add(attachment);
    		}
    	}
    	return fullFilePaths;
    }
}
