package com.azure.spring.samples.model;

import java.util.Objects;

public class AttachmentData extends BaseData {
	
	private String receivedTime;
	private String receivedTimeFolder;
	private String sender;
	private String modelType;
	private String attachmentName;
	private String categories;
	
	public AttachmentData() {
		super();
		// TODO Auto-generated constructor stub
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

	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getCategories() {
		return categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(attachmentName, categories, modelType, receivedTime, receivedTimeFolder, sender);
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
		AttachmentData other = (AttachmentData) obj;
		return Objects.equals(attachmentName, other.attachmentName) && Objects.equals(categories, other.categories)
				&& Objects.equals(modelType, other.modelType) && Objects.equals(receivedTime, other.receivedTime)
				&& Objects.equals(receivedTimeFolder, other.receivedTimeFolder) && Objects.equals(sender, other.sender);
	}

	@Override
	public String toString() {
		return "AttachmentData [receivedTime=" + receivedTime + ", receivedTimeFolder=" + receivedTimeFolder
				+ ", sender=" + sender + ", modelType=" + modelType + ", attachmentName=" + attachmentName
				+ ", categories=" + categories + ", toString()=" + super.toString() + "]";
	}

}
