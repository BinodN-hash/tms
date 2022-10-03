package com.tms.controller.web;

import com.tms.util.ApplicationHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class LoginController {

	@GetMapping({"","/","/login"})
	public String index() {
		return "redirect:/tms/login";
	}

	@GetMapping("/tms/login")
	public String login() {
		if (ApplicationHelper.isAuthenticated()) {
	        return "redirect:/tms/dashboard";
	    }
		return "user-management/login";
	}
	
	@GetMapping("/tms/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			new SecurityContextLogoutHandler().logout(request, response, authentication); // <= This is the call you are
																							// looking for.
		}
		return "redirect:/tms/login";
	}
}
