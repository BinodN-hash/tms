package com.tms.util;

import com.tms.model.User;
import com.tms.service.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

//@Component
public class LoggedUser {

	public static User getUser() {
		CustomUserDetails customUserDetails = (CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return customUserDetails.getUser();
	}
	
	public static User getWebUser() {
		return (User)SecurityContextHolder.getContext().getAuthentication().getDetails();
	}
	
}
