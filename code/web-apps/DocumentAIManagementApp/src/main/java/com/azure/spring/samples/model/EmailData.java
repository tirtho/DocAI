package com.azure.spring.samples.model;

import java.util.Objects;

public class EmailData extends BaseData {

	private String receivedTime;
	private String receivedTimeFolder;
	private String sender;
	private Boolean isHTML;
	private String bodyPreview;
	private String subject;
	private Boolean hasAttachment;
	

	public EmailData() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public EmailData(String id, String upsertTime, String messageId, String messageType, String categories, String url,
			String _rid, String _self, String _etag, String _attachments, Long _ts, 
			String receivedTime, String receivedTimeFolder, String sender, Boolean isHTML, String bodyPreview,
			String subject, Boolean hasAttachment) {
		super(id, upsertTime, messageId, messageType, categories, url, _rid, _self, _etag, _attachments, _ts);
		this.receivedTime = receivedTime;
		this.receivedTimeFolder = receivedTimeFolder;
		this.sender = sender;
		this.isHTML = isHTML;
		this.bodyPreview = bodyPreview;
		this.subject = subject;
		this.hasAttachment = hasAttachment;

	}

	public String getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(String receivedTime) {
		this.receivedTime = receivedTime;
	}

	public String getReceivedTimeFolder() {
		return receivedTimeFolder;
	}

	public void setReceivedTimeFolder(String receivedTimeFolder) {
		this.receivedTimeFolder = receivedTimeFolder;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public Boolean getIsHTML() {
		return isHTML;
	}

	public void setIsHTML(Boolean isHTML) {
		this.isHTML = isHTML;
	}

	public String getBodyPreview() {
		return bodyPreview;
	}

	public void setBodyPreview(String bodyPreview) {
		this.bodyPreview = bodyPreview;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Boolean getHasAttachment() {
		return hasAttachment;
	}

	public void setHasAttachment(Boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(bodyPreview, hasAttachment, isHTML, receivedTime, receivedTimeFolder, sender, subject);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmailData other = (EmailData) obj;
		return Objects.equals(bodyPreview, other.bodyPreview) && Objects.equals(hasAttachment, other.hasAttachment)
				&& Objects.equals(isHTML, other.isHTML) && Objects.equals(receivedTime, other.receivedTime)
				&& Objects.equals(receivedTimeFolder, other.receivedTimeFolder) && Objects.equals(sender, other.sender)
				&& Objects.equals(subject, other.subject);
	}

	@Override
	public String toString() {
		return "EmailData [receivedTime=" + receivedTime + ", receivedTimeFolder=" + receivedTimeFolder + ", sender="
				+ sender + ", isHTML=" + isHTML + ", bodyPreview=" + bodyPreview + ", subject=" + subject
				+ ", hasAttachment=" + hasAttachment + ", toString()=" + super.toString() + "]";
	}
	
}
