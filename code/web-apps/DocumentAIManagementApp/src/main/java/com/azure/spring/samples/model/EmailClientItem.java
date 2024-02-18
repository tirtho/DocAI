package com.azure.spring.samples.model;

import java.util.List;
import java.util.Objects;

public class EmailClientItem {

	// Demo User Full Name 
	private String username;
	// Demo USer Email Address
	private String sender;
	private String subject;
	private String body;
	// Receiver Email Address, i.e. the mailbox that will trigger Logic App
	private String receiver;
	private List<String> attachments;
	private String creationTime;
	
	public EmailClientItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public List<String> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(attachments, body, creationTime, receiver, sender, subject, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmailClientItem other = (EmailClientItem) obj;
		return Objects.equals(attachments, other.attachments) && Objects.equals(body, other.body)
				&& Objects.equals(creationTime, other.creationTime) && Objects.equals(receiver, other.receiver)
				&& Objects.equals(sender, other.sender) && Objects.equals(subject, other.subject)
				&& Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		return "EmailClientItem [username=" + username + ", sender=" + sender + ", subject=" + subject + ", body="
				+ body + ", receiver=" + receiver + ", attachments=" + attachments + ", creationTime=" + creationTime
				+ "]";
	}
	

}
