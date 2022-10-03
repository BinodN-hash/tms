package com.tms.security.filter;

import com.tms.security.token.CustomWebAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CustomWebAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;

	public CustomWebAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		log.info("Username is: {}", username);
		CustomWebAuthenticationToken authenticationToken = new CustomWebAuthenticationToken(username, password);
		Authentication authenticate = authenticationManager.authenticate(authenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authenticate);
		return authenticate;
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException, ServletException {
		log.info("Authentication Successful!");
		response.sendRedirect(request.getContextPath() + "/tms/dashboard");
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		final FlashMap flashMap = new FlashMap();
		// Don't send the AuthenticationException object itself because it has no
		// default constructor and cannot be re-instantiated.
		flashMap.put("error", failed.getMessage());
		final FlashMapManager flashMapManager = new SessionFlashMapManager();
		flashMapManager.saveOutputFlashMap(flashMap, request, response);

		response.sendRedirect(request.getContextPath() + "/tms/login");
	}
}
