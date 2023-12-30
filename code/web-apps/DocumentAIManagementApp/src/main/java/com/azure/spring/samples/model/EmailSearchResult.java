package com.azure.spring.samples.model;

import java.util.List;
import java.util.Objects;

public class EmailSearchResult extends BaseSearchResult {
		
	private Boolean isHTML;
	private String bodyPreview;
	private String subject;
	private Boolean hasAttachment;
	private String sender;
	private String receivedTime;
	
	private List<AttachmentSearchResult> attachments;

	public EmailSearchResult() {
		super();
		// TODO Auto-generated constructor stub
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

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(String receivedTime) {
		this.receivedTime = receivedTime;
	}

	public List<AttachmentSearchResult> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentSearchResult> attachments) {
		this.attachments = attachments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(attachments, bodyPreview, hasAttachment, isHTML, receivedTime, sender, subject);
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
		EmailSearchResult other = (EmailSearchResult) obj;
		return Objects.equals(attachments, other.attachments) && Objects.equals(bodyPreview, other.bodyPreview)
				&& Objects.equals(hasAttachment, other.hasAttachment) && Objects.equals(isHTML, other.isHTML)
				&& Objects.equals(receivedTime, other.receivedTime) && Objects.equals(sender, other.sender)
				&& Objects.equals(subject, other.subject);
	}

	@Override
	public String toString() {
		return "EmailSearchResult [isHTML=" + isHTML + ", bodyPreview=" + bodyPreview + ", subject=" + subject
				+ ", hasAttachment=" + hasAttachment + ", sender=" + sender + ", receivedTime=" + receivedTime
				+ ", attachments=" + attachments + ", toString()=" + super.toString() + "]";
	}

}