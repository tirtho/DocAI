package com.azure.spring.samples.model;

import java.util.List;

public class SearchResult {

	// TODO make it enum
	public static final String SEARCH_TYPE_EMAIL_EXTRACT = "EmailExtract";
	public static String SEARCH_TYPE_BING = "BingSearch";
	public static String SEARCH_TYPE_TWITTER = "TwitterSearch";
	public static String SEARCH_TYPE_LINKEDIN = "LinkedIn";
	private String searchType;
	private String searchResultString;
	private String searchId;
	private String searchResultUrl;
	private String searchResultUrlName;
	private List<String> keyPhrases;
	private String documentAIManagementReview;
	
	public SearchResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SearchResult(String searchType, String searchResultString, String searchId, String searchResultUrl,
			String searchResultUrlName, List<String> keyPhrases, String documentAIManagementReview) {
		super();
		this.searchType = searchType;
		this.searchResultString = searchResultString;
		this.searchId = searchId;
		this.searchResultUrl = searchResultUrl;
		this.searchResultUrlName = searchResultUrlName;
		this.keyPhrases = keyPhrases;
		this.documentAIManagementReview = documentAIManagementReview;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	public String getSearchResultString() {
		return searchResultString;
	}

	public void setSearchResultString(String searchResultString) {
		this.searchResultString = searchResultString;
	}

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public String getSearchResultUrl() {
		return searchResultUrl;
	}

	public void setSearchResultUrl(String searchResultUrl) {
		this.searchResultUrl = searchResultUrl;
	}

	public String getSearchResultUrlName() {
		return searchResultUrlName;
	}

	public void setSearchResultUrlName(String searchResultUrlName) {
		this.searchResultUrlName = searchResultUrlName;
	}

	public List<String> getKeyPhrases() {
		return keyPhrases;
	}

	public void setKeyPhrases(List<String> keyPhrases) {
		this.keyPhrases = keyPhrases;
	}

	public String getDocumentAIManagementReview() {
		return documentAIManagementReview;
	}

	public void setDocumentAIManagementReview(String documentAIManagementReview) {
		this.documentAIManagementReview = documentAIManagementReview;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((documentAIManagementReview == null) ? 0 : documentAIManagementReview.hashCode());
		result = prime * result + ((keyPhrases == null) ? 0 : keyPhrases.hashCode());
		result = prime * result + ((searchId == null) ? 0 : searchId.hashCode());
		result = prime * result + ((searchResultString == null) ? 0 : searchResultString.hashCode());
		result = prime * result + ((searchResultUrl == null) ? 0 : searchResultUrl.hashCode());
		result = prime * result + ((searchResultUrlName == null) ? 0 : searchResultUrlName.hashCode());
		result = prime * result + ((searchType == null) ? 0 : searchType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchResult other = (SearchResult) obj;
		if (documentAIManagementReview == null) {
			if (other.documentAIManagementReview != null)
				return false;
		} else if (!documentAIManagementReview.equals(other.documentAIManagementReview))
			return false;
		if (keyPhrases == null) {
			if (other.keyPhrases != null)
				return false;
		} else if (!keyPhrases.equals(other.keyPhrases))
			return false;
		if (searchId == null) {
			if (other.searchId != null)
				return false;
		} else if (!searchId.equals(other.searchId))
			return false;
		if (searchResultString == null) {
			if (other.searchResultString != null)
				return false;
		} else if (!searchResultString.equals(other.searchResultString))
			return false;
		if (searchResultUrl == null) {
			if (other.searchResultUrl != null)
				return false;
		} else if (!searchResultUrl.equals(other.searchResultUrl))
			return false;
		if (searchResultUrlName == null) {
			if (other.searchResultUrlName != null)
				return false;
		} else if (!searchResultUrlName.equals(other.searchResultUrlName))
			return false;
		if (searchType == null) {
			if (other.searchType != null)
				return false;
		} else if (!searchType.equals(other.searchType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SearchResult [searchType=" + searchType + ", searchResultString=" + searchResultString + ", searchId="
				+ searchId + ", searchResultUrl=" + searchResultUrl + ", searchResultUrlName=" + searchResultUrlName
				+ ", keyPhrases=" + keyPhrases + ", documentAIManagementReview=" + documentAIManagementReview + "]";
	}

}
