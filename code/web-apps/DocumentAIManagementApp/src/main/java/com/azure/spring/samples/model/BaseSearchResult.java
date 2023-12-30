package com.azure.spring.samples.model;

import java.util.Objects;

public class BaseSearchResult {

	private String id;
	private String messageId;
	private String upsertTime;
	private String messageType;
	private String categories;
	private String url;
	
	public BaseSearchResult() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public BaseSearchResult(String id, String messageId, String upsertTime, String messageType, String categories,
			String url) {
		super();
		this.id = id;
		this.messageId = messageId;
		this.upsertTime = upsertTime;
		this.messageType = messageType;
		this.categories = categories;
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getUpsertTime() {
		return upsertTime;
	}

	public void setUpsertTime(String upsertTime) {
		this.upsertTime = upsertTime;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getCategories() {
		return categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		return Objects.hash(categories, id, messageId, messageType, upsertTime, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseSearchResult other = (BaseSearchResult) obj;
		return Objects.equals(categories, other.categories) && Objects.equals(id, other.id)
				&& Objects.equals(messageId, other.messageId) && Objects.equals(messageType, other.messageType)
				&& Objects.equals(upsertTime, other.upsertTime) && Objects.equals(url, other.url);
	}

	@Override
	public String toString() {
		return "BaseSearchResult [id=" + id + ", messageId=" + messageId + ", upsertTime=" + upsertTime
				+ ", messageType=" + messageType + ", categories=" + categories + ", url=" + url + "]";
	}

}
