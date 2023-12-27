package com.azure.spring.samples.model;

import java.util.HashMap;
import java.util.Objects;

public class FormTableRow {

	// TODO: May need to add more fields like confidence or a data type for value etc...
	
	private HashMap<String, String> columns;
	
	public FormTableRow() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FormTableRow(HashMap<String, String> columns) {
		super();
		this.columns = columns;
	}

	public HashMap<String, String> getColumns() {
		return columns;
	}

	public void setColumns(HashMap<String, String> columns) {
		this.columns = columns;
	}

	@Override
	public int hashCode() {
		return Objects.hash(columns);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormTableRow other = (FormTableRow) obj;
		return Objects.equals(columns, other.columns);
	}

	@Override
	public String toString() {
		return "FormTableRow [columns=" + columns + "]";
	}

}
