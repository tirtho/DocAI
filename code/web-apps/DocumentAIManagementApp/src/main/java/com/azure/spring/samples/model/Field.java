package com.azure.spring.samples.model;

import java.util.Objects;

public class Field {

	private String fieldName;
	private String fieldValueType;
	private Double fieldConfidence;
	private String fieldValue;
	
	public Field() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Field(String fieldName, String fieldValueType, Double fieldConfidence, String fieldValue) {
		super();
		this.fieldName = fieldName;
		this.fieldValueType = fieldValueType;
		this.fieldConfidence = fieldConfidence;
		this.fieldValue = fieldValue;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldValueType() {
		return fieldValueType;
	}

	public void setFieldValueType(String fieldValueType) {
		this.fieldValueType = fieldValueType;
	}

	public Double getFieldConfidence() {
		return fieldConfidence;
	}

	public void setFieldConfidence(Double fieldConfidence) {
		this.fieldConfidence = fieldConfidence;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fieldConfidence, fieldName, fieldValue, fieldValueType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Field other = (Field) obj;
		return Objects.equals(fieldConfidence, other.fieldConfidence) && Objects.equals(fieldName, other.fieldName)
				&& Objects.equals(fieldValue, other.fieldValue) && Objects.equals(fieldValueType, other.fieldValueType);
	}

	@Override
	public String toString() {
		return "Field [fieldName=" + fieldName + ", fieldValueType=" + fieldValueType + ", fieldConfidence="
				+ fieldConfidence + ", fieldValue=" + fieldValue + "]";
	}

}
