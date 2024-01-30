package com.azure.spring.samples.utils;

import java.util.Objects;

public class ReturnEntity<U, V> {

	private U status;
	private V entity;
	
	/**
	 * 
	 * @param status
	 * @param entity
	 */
	public ReturnEntity(U status, V entity) {
		super();
		this.status = status;
		this.entity = entity;
	}

	public U getStatus() {
		return status;
	}

	public void setStatus(U status) {
		this.status = status;
	}

	public V getEntity() {
		return entity;
	}

	public void setEntity(V entity) {
		this.entity = entity;
	}

	@Override
	public int hashCode() {
		return Objects.hash(entity, status);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReturnEntity<?, ?> other = (ReturnEntity<?, ?>) obj;
		return Objects.equals(entity, other.entity) && Objects.equals(status, other.status);
	}

	@Override
	public String toString() {
		return "ReturnEntity [status=" + status + ", entity=" + entity + "]";
	}
}
