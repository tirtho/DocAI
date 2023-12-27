package com.azure.spring.samples.model;

import java.util.ArrayList;
import java.util.Objects;

public class AttachmentExtractData {

	private String id;
	private String upsertTime;
	private String messageId;
	private String messageType;
	private String attachmentName;
	private String url;
	private String frAPIVersion;
	private String modelId;
	private Boolean isHandwritten;
	
	private ArrayList<ExtractData> extracts;

	public AttachmentExtractData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AttachmentExtractData(String id, String upsertTime, String messageId, String messageType,
			String attachmentName, String url, String frAPIVersion, String modelId, Boolean isHandwritten,
			ArrayList<ExtractData> extracts) {
		super();
		this.id = id;
		this.upsertTime = upsertTime;
		this.messageId = messageId;
		this.messageType = messageType;
		this.attachmentName = attachmentName;
		this.url = url;
		this.frAPIVersion = frAPIVersion;
		this.modelId = modelId;
		this.isHandwritten = isHandwritten;
		this.extracts = extracts;
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

	public String getFrAPIVersion() {
		return frAPIVersion;
	}

	public void setFrAPIVersion(String frAPIVersion) {
		this.frAPIVersion = frAPIVersion;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public Boolean getIsHandwritten() {
		return isHandwritten;
	}

	public void setIsHandwritten(Boolean isHandwritten) {
		this.isHandwritten = isHandwritten;
	}

	public ArrayList<ExtractData> getExtracts() {
		return extracts;
	}

	public void setExtracts(ArrayList<ExtractData> extracts) {
		this.extracts = extracts;
	}

	@Override
	public int hashCode() {
		return Objects.hash(attachmentName, extracts, frAPIVersion, id, isHandwritten, messageId, messageType, modelId,
				upsertTime, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttachmentExtractData other = (AttachmentExtractData) obj;
		return Objects.equals(attachmentName, other.attachmentName) && Objects.equals(extracts, other.extracts)
				&& Objects.equals(frAPIVersion, other.frAPIVersion) && Objects.equals(id, other.id)
				&& Objects.equals(isHandwritten, other.isHandwritten) && Objects.equals(messageId, other.messageId)
				&& Objects.equals(messageType, other.messageType) && Objects.equals(modelId, other.modelId)
				&& Objects.equals(upsertTime, other.upsertTime) && Objects.equals(url, other.url);
	}

	@Override
	public String toString() {
		return "AttachmentExtractData [id=" + id + ", upsertTime=" + upsertTime + ", messageId=" + messageId
				+ ", messageType=" + messageType + ", attachmentName=" + attachmentName + ", url=" + url
				+ ", frAPIVersion=" + frAPIVersion + ", modelId=" + modelId + ", isHandwritten=" + isHandwritten
				+ ", extracts=" + extracts + "]";
	}

	
}
