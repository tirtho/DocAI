package com.azure.spring.samples.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExtractData {

	private Integer documentId;
	private String documentType;
	private Double documentConfidence;
	private List<Map<String, ?>> fields;
	private List<Map<String, ?>> tables;

	public ExtractData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public Double getDocumentConfidence() {
		return documentConfidence;
	}

	public void setDocumentConfidence(Double documentConfidence) {
		this.documentConfidence = documentConfidence;
	}

	public List<Map<String, ?>> getFields() {
		return fields;
	}

	public void setFields(List<Map<String, ?>> fields) {
		this.fields = fields;
	}

	public List<Map<String, ?>> getTables() {
		return tables;
	}

	public void setTables(List<Map<String, ?>> tables) {
		this.tables = tables;
	}

	@Override
	public int hashCode() {
		return Objects.hash(documentConfidence, documentId, documentType, fields, tables);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExtractData other = (ExtractData) obj;
		return Objects.equals(documentConfidence, other.documentConfidence)
				&& Objects.equals(documentId, other.documentId) && Objects.equals(documentType, other.documentType)
				&& Objects.equals(fields, other.fields) && Objects.equals(tables, other.tables);
	}

	@Override
	public String toString() {
		return "ExtractData [documentId=" + documentId + ", documentType=" + documentType + ", documentConfidence="
				+ documentConfidence + ", fields=" + fields + ", tables=" + tables + ", toString()=" + super.toString()
				+ "]";
	}
	
}
