package com.azure.spring.samples;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.spring.samples.model.LoggedInUserProfile;
import com.azure.spring.samples.utils.ReturnEntity;

public class DocumentAIManagementAppAuthorization {

	private static final String PREFERRED_USERNAME_KEY = "preferred_username";

	public DocumentAIManagementAppAuthorization() {
		super();
	}

	public static ReturnEntity<Boolean, LoggedInUserProfile> authorizeUser(OAuth2AuthenticationToken authToken, String demoUsers) {
		
		String userName = "";
		String preferredUserName = "";
		
		if (authToken != null) {
			OAuth2User auth2User = authToken.getPrincipal();
			userName = auth2User.getName();
			preferredUserName = auth2User.getAttribute(PREFERRED_USERNAME_KEY);
			String [] allTheDemoUsers = StringUtils.split(demoUsers, ",");
			if (preferredUserName != null && allTheDemoUsers != null && allTheDemoUsers.length > 0) {
				// Authorize
				for (String demoUser : allTheDemoUsers) {
					if (StringUtils.compare(demoUser.toLowerCase(), preferredUserName.toLowerCase()) == 0) {
						// Current logged in user is in the demo user list. Authorized!
						return new ReturnEntity<Boolean, LoggedInUserProfile>(true, new LoggedInUserProfile(userName, preferredUserName));
					}
				}
			}
		}
		return new ReturnEntity<Boolean, LoggedInUserProfile>(false, new LoggedInUserProfile(userName, preferredUserName));
	}
}
