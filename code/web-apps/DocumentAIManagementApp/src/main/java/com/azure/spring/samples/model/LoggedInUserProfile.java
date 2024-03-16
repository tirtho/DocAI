/**
 * 
 */
package com.azure.spring.samples.model;

import java.util.Objects;

/**
 * 
 */
public class LoggedInUserProfile {

	String userName;
	String preferredUserName;
	
	public LoggedInUserProfile() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LoggedInUserProfile(String userName, String preferredUserName) {
		super();
		this.userName = userName;
		this.preferredUserName = preferredUserName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPreferredUserName() {
		return preferredUserName;
	}

	public void setPreferredUserName(String preferredUserName) {
		this.preferredUserName = preferredUserName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(preferredUserName, userName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LoggedInUserProfile other = (LoggedInUserProfile) obj;
		return Objects.equals(preferredUserName, other.preferredUserName) && Objects.equals(userName, other.userName);
	}

	@Override
	public String toString() {
		return "LoggedInUserProfile [userName=" + userName + ", preferredUserName=" + preferredUserName + "]";
	}
	
}
