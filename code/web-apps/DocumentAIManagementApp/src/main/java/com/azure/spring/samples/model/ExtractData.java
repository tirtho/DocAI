package com.azure.spring.samples.model;

import java.util.ArrayList;
import java.util.Objects;

public class ExtractData {

	private ArrayList<FormField> formFields;
	private ArrayList<FormTable> formTables;
	
	public ExtractData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ExtractData(ArrayList<FormField> formFields, ArrayList<FormTable> formTables) {
		super();
		this.formFields = formFields;
		this.formTables = formTables;
	}

	public ArrayList<FormField> getFormFields() {
		return formFields;
	}

	public void setFormFields(ArrayList<FormField> formFields) {
		this.formFields = formFields;
	}

	public ArrayList<FormTable> getFormTables() {
		return formTables;
	}

	public void setFormTables(ArrayList<FormTable> formTables) {
		this.formTables = formTables;
	}

	@Override
	public int hashCode() {
		return Objects.hash(formFields, formTables);
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
		return Objects.equals(formFields, other.formFields) && Objects.equals(formTables, other.formTables);
	}

	@Override
	public String toString() {
		return "ExtractData [formFields=" + formFields + ", formTables=" + formTables + "]";
	}
	
}
