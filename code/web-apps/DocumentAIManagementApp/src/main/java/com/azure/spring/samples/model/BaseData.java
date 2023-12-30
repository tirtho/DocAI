package com.azure.spring.samples.model;

import java.util.Objects;

public class BaseData {
	private String id;
	private String upsertTime;
	private String messageId;
	private String messageType;
	private String categories;
	private String url;

	private String _rid;
	private String _self;
	private String _etag;
	private String _attachments;
	private Long _ts;
	
	public BaseData() {
		super();
		// TODO Auto-generated constructor stub
	}
	public BaseData(String id, String upsertTime, String messageId, String messageType, String categories, String url,
			String _rid, String _self, String _etag, String _attachments, Long _ts) {
		super();
		this.id = id;
		this.upsertTime = upsertTime;
		this.messageId = messageId;
		this.messageType = messageType;
		this.categories = categories;
		this.url = url;
		this._rid = _rid;
		this._self = _self;
		this._etag = _etag;
		this._attachments = _attachments;
		this._ts = _ts;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUpsertTime() {
		return upsertTime;
	}
	public void setUpsertTime(String upsertTime) {
		this.upsertTime = upsertTime;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
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
	public String get_rid() {
		return _rid;
	}
	public void set_rid(String _rid) {
		this._rid = _rid;
	}
	public String get_self() {
		return _self;
	}
	public void set_self(String _self) {
		this._self = _self;
	}
	public String get_etag() {
		return _etag;
	}
	public void set_etag(String _etag) {
		this._etag = _etag;
	}
	public String get_attachments() {
		return _attachments;
	}
	public void set_attachments(String _attachments) {
		this._attachments = _attachments;
	}
	public Long get_ts() {
		return _ts;
	}
	public void set_ts(Long _ts) {
		this._ts = _ts;
	}
	@Override
	public int hashCode() {
		return Objects.hash(_attachments, _etag, _rid, _self, _ts, categories, id, messageId, messageType, upsertTime,
				url);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseData other = (BaseData) obj;
		return Objects.equals(_attachments, other._attachments) && Objects.equals(_etag, other._etag)
				&& Objects.equals(_rid, other._rid) && Objects.equals(_self, other._self)
				&& Objects.equals(_ts, other._ts) && Objects.equals(categories, other.categories)
				&& Objects.equals(id, other.id) && Objects.equals(messageId, other.messageId)
				&& Objects.equals(messageType, other.messageType) && Objects.equals(upsertTime, other.upsertTime)
				&& Objects.equals(url, other.url);
	}
	@Override
	public String toString() {
		return "BaseData [id=" + id + ", upsertTime=" + upsertTime + ", messageId=" + messageId + ", messageType="
				+ messageType + ", categories=" + categories + ", url=" + url + ", _rid=" + _rid + ", _self=" + _self
				+ ", _etag=" + _etag + ", _attachments=" + _attachments + ", _ts=" + _ts + "]";
	}

	
}
