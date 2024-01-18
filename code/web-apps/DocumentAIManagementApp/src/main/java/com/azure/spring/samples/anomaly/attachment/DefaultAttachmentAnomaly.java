package com.azure.spring.samples.anomaly.attachment;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.samples.anomaly.AttachmentAnomaly;
import com.azure.spring.samples.aoai.AzureOpenAIOperation;
import com.azure.spring.samples.cosmosdb.CosmosDBOperation;

public class DefaultAttachmentAnomaly implements AttachmentAnomaly {

    public static Logger logger = LoggerFactory.getLogger(DefaultAttachmentAnomaly.class);
    
	public static String FIND_MISSING_FIELDS_PROMT = "";

    private CosmosDBOperation cosmosDB;
    private AzureOpenAIOperation aoaiOps;
	   
    public DefaultAttachmentAnomaly(
    				CosmosDBOperation cosmosDB,
    				AzureOpenAIOperation aoaiOps
    			) {
		super();
		this.cosmosDB = cosmosDB;
		this.aoaiOps = aoaiOps;
	}

	@Override
	public List<?> getAttachmentAnomaly(String attachmentId) {
		// TODO Auto-generated method stub
		List<String> reviewSummary = new ArrayList<>();
		reviewSummary.add("Note: DefaultAttachmentAnomaly: NotYetImplemented");
		return reviewSummary;
	}

}
