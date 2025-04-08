package com.azure.spring.samples.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

public class AttachmentExtractsData extends BaseData {

	private String attachmentName;
	private String frAPIVersion;
	private String modelId;
	private Boolean isHandwritten;
	private String operationId;
	private String operationStatus;
	
	
	private List<ExtractData> extracts;

	public AttachmentExtractsData() {
		super();
		// TODO Auto-generated constructor stub
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
	
	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public String getOperationStatus() {
		return operationStatus;
	}

	public void setOperationStatus(String operationStatus) {
		this.operationStatus = operationStatus;
	}
	
	@Override
	public String toString() {
		return "AttachmentExtractsData [attachmentName=" + attachmentName + ", frAPIVersion=" + frAPIVersion
				+ ", modelId=" + modelId + ", isHandwritten=" + isHandwritten + ", operationId=" + operationId
				+ ", operationStatus=" + operationStatus + ", extracts=" + extracts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(attachmentName, extracts, frAPIVersion, isHandwritten, modelId,
				operationId, operationStatus);
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
		AttachmentExtractsData other = (AttachmentExtractsData) obj;
		return Objects.equals(attachmentName, other.attachmentName) && Objects.equals(extracts, other.extracts)
				&& Objects.equals(frAPIVersion, other.frAPIVersion)
				&& Objects.equals(isHandwritten, other.isHandwritten) && Objects.equals(modelId, other.modelId)
				&& Objects.equals(operationId, other.operationId)
				&& Objects.equals(operationStatus, other.operationStatus);
	}

	public String getFieldValue(String valueOfFieldName) {
		for (ExtractData ed : extracts) {
			for (Map<String, ?> f : ed.getFields()) {
				if (StringUtils.compare(valueOfFieldName, (String) f.get("fieldName")) == 0) {
					return StringUtils.trimToNull((String) f.get("fieldValue"));
				}
			}
		}
		return null;
	}
	
}
