package com.azure.spring.samples.model;

import java.util.ArrayList;
import java.util.Objects;

public class EmailExtractData {

	private String id;
	private String upsertTime;
	private String messageId;
	private String messageType;
	private String receivedTime;
	private String receivedTimeFolder;
	private String sender;
	private ArrayList<Category> categories;
	private String url;
	private Boolean isHTML;
	private String bodyPreview;
	private String subject;
	private Boolean hasAttachment;
	private String _rid;
	private String _self;
	private String _etag;
	private String _attachments;
	private Long _ts;
	
	public EmailExtractData() {
		super();
		// TODO Auto-generated constructor stub
	}

	public EmailExtractData(String id, String upsertTime, String messageId, String messageType, String receivedTime,
			String receivedTimeFolder, String sender, ArrayList<Category> categories, String url, Boolean isHTML,
			String bodyPreview, String subject, Boolean hasAttachment, String _rid, String _self, String _etag,
			String _attachments, Long _ts) {
		super();
		this.id = id;
		this.upsertTime = upsertTime;
		this.messageId = messageId;
		this.messageType = messageType;
		this.receivedTime = receivedTime;
		this.receivedTimeFolder = receivedTimeFolder;
		this.sender = sender;
		this.categories = categories;
		this.url = url;
		this.isHTML = isHTML;
		this.bodyPreview = bodyPreview;
		this.subject = subject;
		this.hasAttachment = hasAttachment;
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

	public String getReceivedTime() {
		return receivedTime;
	}

	public void setReceivedTime(String receivedTime) {
		this.receivedTime = receivedTime;
	}

	public String getReceivedTimeFolder() {
		return receivedTimeFolder;
	}

	public void setReceivedTimeFolder(String receivedTimeFolder) {
		this.receivedTimeFolder = receivedTimeFolder;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public ArrayList<Category> getCategories() {
		return categories;
	}

	public void setCategories(ArrayList<Category> categories) {
		this.categories = categories;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getIsHTML() {
		return isHTML;
	}

	public void setIsHTML(Boolean isHTML) {
		this.isHTML = isHTML;
	}

	public String getBodyPreview() {
		return bodyPreview;
	}

	public void setBodyPreview(String bodyPreview) {
		this.bodyPreview = bodyPreview;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Boolean getHasAttachment() {
		return hasAttachment;
	}

	public void setHasAttachment(Boolean hasAttachment) {
		this.hasAttachment = hasAttachment;
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
		return Objects.hash(_attachments, _etag, _rid, _self, _ts, bodyPreview, categories, hasAttachment, id, isHTML,
				messageId, messageType, receivedTime, receivedTimeFolder, sender, subject, upsertTime, url);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmailExtractData other = (EmailExtractData) obj;
		return Objects.equals(_attachments, other._attachments) && Objects.equals(_etag, other._etag)
				&& Objects.equals(_rid, other._rid) && Objects.equals(_self, other._self)
				&& Objects.equals(_ts, other._ts) && Objects.equals(bodyPreview, other.bodyPreview)
				&& Objects.equals(categories, other.categories) && Objects.equals(hasAttachment, other.hasAttachment)
				&& Objects.equals(id, other.id) && Objects.equals(isHTML, other.isHTML)
				&& Objects.equals(messageId, other.messageId) && Objects.equals(messageType, other.messageType)
				&& Objects.equals(receivedTime, other.receivedTime)
				&& Objects.equals(receivedTimeFolder, other.receivedTimeFolder) && Objects.equals(sender, other.sender)
				&& Objects.equals(subject, other.subject) && Objects.equals(upsertTime, other.upsertTime)
				&& Objects.equals(url, other.url);
	}

	@Override
	public String toString() {
		return "EmailExtractData [id=" + id + ", upsertTime=" + upsertTime + ", messageId=" + messageId
				+ ", messageType=" + messageType + ", receivedTime=" + receivedTime + ", receivedTimeFolder="
				+ receivedTimeFolder + ", sender=" + sender + ", categories=" + categories + ", url=" + url
				+ ", isHTML=" + isHTML + ", bodyPreview=" + bodyPreview + ", subject=" + subject + ", hasAttachment="
				+ hasAttachment + ", _rid=" + _rid + ", _self=" + _self + ", _etag=" + _etag + ", _attachments="
				+ _attachments + ", _ts=" + _ts + "]";
	}	
	
}
