package com.azure.spring.samples.model;

import java.util.ArrayList;
import java.util.Objects;

public class FormTable {

	private String name;
	private ArrayList<FormTableRow> rows;
	
	public FormTable() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FormTable(String name, ArrayList<FormTableRow> rows) {
		super();
		this.name = name;
		this.rows = rows;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<FormTableRow> getRows() {
		return rows;
	}

	public void setRows(ArrayList<FormTableRow> rows) {
		this.rows = rows;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, rows);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormTable other = (FormTable) obj;
		return Objects.equals(name, other.name) && Objects.equals(rows, other.rows);
	}

	@Override
	public String toString() {
		return "FormTable [name=" + name + ", rows=" + rows + "]";
	}

}
