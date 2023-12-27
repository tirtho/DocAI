package com.azure.spring.samples.model;

import java.util.ArrayList;
import java.util.Objects;

public class FormField {

	private Integer documentId;
	private Double confidence;
	private ArrayList<Field> fields;
	
	public FormField() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FormField(Integer documentId, Double confidence, ArrayList<Field> fields) {
		super();
		this.documentId = documentId;
		this.confidence = confidence;
		this.fields = fields;
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	public Double getConfidence() {
		return confidence;
	}

	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}

	public ArrayList<Field> getFields() {
		return fields;
	}

	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}

	@Override
	public int hashCode() {
		return Objects.hash(confidence, documentId, fields);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormField other = (FormField) obj;
		return Objects.equals(confidence, other.confidence) && Objects.equals(documentId, other.documentId)
				&& Objects.equals(fields, other.fields);
	}

	@Override
	public String toString() {
		return "FormField [documentId=" + documentId + ", confidence=" + confidence + ", fields=" + fields + "]";
	}

}
