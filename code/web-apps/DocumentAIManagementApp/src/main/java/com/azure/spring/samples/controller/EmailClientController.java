// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.samples.controller;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.model.EmailClientItem;

import core.azure.spring.samples.messaging.SendOutlookEmail;

@RestController
public class EmailClientController {

    private static Logger logger = LoggerFactory.getLogger(EmailClientController.class);
   
    @Value("${azure.cosmos.uri}")
    private String azureCosmosURI;
    @Value("${azure.cosmos.key}")
    private String azureCosmosKey;
    @Value("${azure.cosmos.database}")
    private String azureCosmosDatabaseName;
    @Value("${azure.cosmos.container.demos}")
    private String azureCosmosContainerDemosName;
    @Value("${docai.receiver.email.address}")
    private String docAIEmailReceiver;
    @Value("${spring.mail.username}")
    private String docAIEmailSender;
    
    @Value("${docai.app.client.id}")
	private String clientId;
    @Value("${docai.app.client.secret}")
	private String clientSecret;
    @Value("${docai.app.tenant.id}")
	private String tenantId;	
    
    private String LOCAL_ATTACHMENTS_FOLDER = "static\\documents\\";
    
    public EmailClientController() {
    }
     
    /**
     * HTTP POST NEW ONE
     */
    @RequestMapping(value = "/api/emailClient", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> emailClient(@RequestBody EmailClientItem emailClientItem) {
        logger.info("POST request access '/api/emailClient' path with item: {}", emailClientItem);
    	String responseMessage;
        try {
        	List<String> filesToAttach = addLocaFilePaths(emailClientItem.getAttachments());
        	List<String> emailReceivers = new ArrayList<>();
        	emailReceivers.add(docAIEmailReceiver);
        	SendOutlookEmail soe = new SendOutlookEmail(clientId, clientSecret, tenantId);
        	soe.sendMailWithAttachments(
        						docAIEmailSender,
        						emailReceivers,
        						emailClientItem.getSubject(), 
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
