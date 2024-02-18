package core.azure.spring.samples.messaging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.Attachment;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.FileAttachment;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.AttachmentCollectionPage;
import com.microsoft.graph.requests.AttachmentCollectionResponse;
import com.microsoft.graph.requests.GraphServiceClient;

public class SendOutlookEmail {

	private String clientId;
	private String clientSecret;
	private String tenantId;	

	public SendOutlookEmail() {
		super();
	}

	public SendOutlookEmail(String clientId, String clientSecret, String tenantId) {
		super();
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.tenantId = tenantId;
	}

	/**
	 * 
	 * @param sender
	 * @param receivers
	 * @param subject
	 * @param body
	 * @param attachments
	 * @throws IOException
	 */
	public void sendMailWithAttachments(
					String sender,
					List<String> receivers,
					String subject,
					String body,
					List<String> attachments
				) throws IOException {
	    final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
	            .clientId(clientId)
	            .clientSecret(clientSecret)
	            .tenantId(tenantId)
	            .build();
	    List<String> scopes = new ArrayList<>();
	    scopes.add("https://graph.microsoft.com/.default");

	    final TokenCredentialAuthProvider tokenCredentialAuthProvider = new TokenCredentialAuthProvider(scopes, clientSecretCredential);
	    final GraphServiceClient<?> graphClient = GraphServiceClient
													.builder()
													.authenticationProvider(tokenCredentialAuthProvider)
													.buildClient();

	    // Add email subject and body
	    Message message = new Message();
	    message.subject = subject;
	    ItemBody itemBody = new ItemBody();
	    itemBody.contentType = BodyType.HTML;
	    itemBody.content = body;
	    message.body = itemBody;
	    
	    // Add attachments
	    if (attachments != null && attachments.size() > 0) {
		    List<Attachment> theAttachments = new ArrayList<>();
	    	for (String attachment : attachments) {
	    		FileAttachment theAttachment = new FileAttachment();
	    		theAttachment.name = attachment;
	    		theAttachment.contentType = "text/plain";
	    		theAttachment.oDataType = "#microsoft.graph.fileAttachment";
	    		theAttachment.contentBytes = Files.readAllBytes(Paths.get(attachment));
	    		theAttachments.add(theAttachment);
	    	}
		    message.hasAttachments = true;
		    AttachmentCollectionResponse attachmentCollectionResponse = new AttachmentCollectionResponse();
		    attachmentCollectionResponse.value = theAttachments;
		    AttachmentCollectionPage attachmentCollectionPage = new AttachmentCollectionPage(attachmentCollectionResponse, null);
		    message.attachments = attachmentCollectionPage;
	    }
	    
	    // Add receivers' email addresses
	    List<Recipient> toList = new ArrayList<>();
	    for (String receiver : receivers) {
	    	Recipient toReceiver = new Recipient();
	    	EmailAddress emailAddress = new EmailAddress();
	    	emailAddress.address = receiver;
	    	toReceiver.emailAddress = emailAddress;
	    	toList.add(toReceiver);
	    }
	    message.toRecipients = toList;
	    
	    graphClient.users(sender)
	    			.sendMail(UserSendMailParameterSet
		                        .newBuilder()
		                        .withMessage(message)
		                        .withSaveToSentItems(true)
		                        .build())
	                .buildRequest()
	                .post();
	    }

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}	
	
}
