package com.azure.spring.samples.model;

import java.util.ArrayList;
import java.util.Objects;

public class AttachmentData {
	
	private String id;
	private String upsertTime;
	private String messageId;
	private String messageType;
	private String receivedTime;
	private String receivedTimeFolder;
	private String sender;
	private ArrayList<Category> categories;
	private String attachmentName;
	private String url;

	public AttachmentData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AttachmentData(String id, String upsertTime, String messageId, String messageType, String receivedTime,
			String receivedTimeFolder, String sender, ArrayList<Category> categories, String attachmentName,
			String url) {
		super();
		this.id = id;
		this.upsertTime = upsertTime;
		this.messageId = messageId;
		this.messageType = messageType;
		this.receivedTime = receivedTime;
		this.receivedTimeFolder = receivedTimeFolder;
		this.sender = sender;
		this.categories = categories;
		this.attachmentName = attachmentName;
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUpsertTime() {
		return upsertTime;
	}

	public void setUpsertTime(String upsertTime) {
		this.upsertTime = upsertTime;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
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

	public ArrayList<Category> getCategories() {
		return categories;
	}

	public void setCategories(ArrayList<Category> categories) {
		this.categories = categories;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		return Objects.hash(attachmentName, categories, id, messageId, messageType, receivedTime, receivedTimeFolder,
				sender, upsertTime, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttachmentData other = (AttachmentData) obj;
		return Objects.equals(attachmentName, other.attachmentName) && Objects.equals(categories, other.categories)
				&& Objects.equals(id, other.id) && Objects.equals(messageId, other.messageId)
				&& Objects.equals(messageType, other.messageType) && Objects.equals(receivedTime, other.receivedTime)
				&& Objects.equals(receivedTimeFolder, other.receivedTimeFolder) && Objects.equals(sender, other.sender)
				&& Objects.equals(upsertTime, other.upsertTime) && Objects.equals(url, other.url);
	}

	@Override
	public String toString() {
		return "AttachmentData [id=" + id + ", upsertTime=" + upsertTime + ", messageId=" + messageId + ", messageType="
				+ messageType + ", receivedTime=" + receivedTime + ", receivedTimeFolder=" + receivedTimeFolder
				+ ", sender=" + sender + ", categories=" + categories + ", attachmentName=" + attachmentName + ", url="
				+ url + "]";
	}

}
