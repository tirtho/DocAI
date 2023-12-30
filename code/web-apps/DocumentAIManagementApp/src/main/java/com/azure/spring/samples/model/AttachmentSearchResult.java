package com.azure.spring.samples.model;

import java.util.List;
import java.util.Objects;

public class AttachmentSearchResult extends BaseSearchResult {

	private String sender;
	private String attachmentName;
	private String frAPIVersion;
	private String modelType;
	private String modelId;
	private Boolean isHandwritten;
	
	private List<ExtractData> extracts;

	public AttachmentSearchResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
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

	public List<ExtractData> getExtracts() {
		return extracts;
	}

	public void setExtracts(List<ExtractData> extracts) {
		this.extracts = extracts;
	}

	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(attachmentName, extracts, frAPIVersion, isHandwritten, modelId, modelType, sender);
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
		AttachmentSearchResult other = (AttachmentSearchResult) obj;
		return Objects.equals(attachmentName, other.attachmentName) && Objects.equals(extracts, other.extracts)
				&& Objects.equals(frAPIVersion, other.frAPIVersion)
				&& Objects.equals(isHandwritten, other.isHandwritten) && Objects.equals(modelId, other.modelId)
				&& Objects.equals(modelType, other.modelType) && Objects.equals(sender, other.sender);
	}

	@Override
	public String toString() {
		return "AttachmentSearchResult [sender=" + sender + ", attachmentName=" + attachmentName + ", frAPIVersion="
				+ frAPIVersion + ", modelType=" + modelType + ", modelId=" + modelId + ", isHandwritten="
				+ isHandwritten + ", extracts=" + extracts + ", toString()=" + super.toString() + "]";
	}

}
