// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.samples.model;

import java.util.Date;
import java.util.List;

//@Container(containerName = "DocumentAIManagementReview")
public class DocumentAIManagementReview {
//    @Id
//    @PartitionKey
    private String searchId;
    private String searchString;
    private String searchType;
    private Date createDate;
    private List<String> keyPhrases;
    private String searchScore;
    
    public DocumentAIManagementReview() {
    }

	public DocumentAIManagementReview(String searchId, String searchString, String searchType, Date createDate,
			List<String> keyPhrases, String searchScore) {
		super();
		this.searchId = searchId;
		this.searchString = searchString;
		this.searchType = searchType;
		this.createDate = createDate;
		this.keyPhrases = keyPhrases;
		this.searchScore = searchScore;
	}

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public List<String> getKeyPhrases() {
		return keyPhrases;
	}

	public void setKeyPhrases(List<String> keyPhrases) {
		this.keyPhrases = keyPhrases;
	}

	public String getSearchScore() {
		return searchScore;
	}

	public void setSearchScore(String searchScore) {
		this.searchScore = searchScore;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
		result = prime * result + ((keyPhrases == null) ? 0 : keyPhrases.hashCode());
		result = prime * result + ((searchId == null) ? 0 : searchId.hashCode());
		result = prime * result + ((searchScore == null) ? 0 : searchScore.hashCode());
		result = prime * result + ((searchString == null) ? 0 : searchString.hashCode());
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
		DocumentAIManagementReview other = (DocumentAIManagementReview) obj;
		if (createDate == null) {
			if (other.createDate != null)
				return false;
		} else if (!createDate.equals(other.createDate))
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
		if (searchScore == null) {
			if (other.searchScore != null)
				return false;
		} else if (!searchScore.equals(other.searchScore))
			return false;
		if (searchString == null) {
			if (other.searchString != null)
				return false;
		} else if (!searchString.equals(other.searchString))
			return false;
		if (searchType == null) {
			if (other.searchType != null)
				return false;
		} else if (!searchType.equals(other.searchType))
			return false;
		return true;
	}
   
}

