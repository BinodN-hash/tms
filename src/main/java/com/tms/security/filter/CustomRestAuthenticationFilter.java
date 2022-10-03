package com.tms.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tms.properties.SetUpProperties;
import com.tms.security.token.CustomRestAuthenticationToken;
import com.tms.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomRestAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	
	private final AuthenticationManager authenticationManager;
	
	private final JwtTokenUtil jwtUtil;
	
	private static SetUpProperties setup = SetUpProperties.getInstance();
	

	public CustomRestAuthenticationFilter(AuthenticationManager authenticationManager,JwtTokenUtil jwtUtil) {

		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}
	
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		log.info("Username is: {}",username);
		CustomRestAuthenticationToken authenticationToken = new CustomRestAuthenticationToken(username, password);
		return authenticationManager.authenticate(authenticationToken);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException, ServletException {
		log.info("Rest Authentication Successful!");
		
		String username = (String) authentication.getPrincipal();
		int accessTokenValidity = setup.getIntProperty("jwt.access.token.validity");
		String accessToken = jwtUtil.generateAccessToken(username,accessTokenValidity);
		String refreshToken = jwtUtil.generateRefreshToken(username);
		
		Map<String,Object> tokens = new HashMap<>();
		tokens.put("access_token", accessToken);
		tokens.put("refresh_token", refreshToken);
		tokens.put("expires_in", accessTokenValidity *60*1000);
		response.setContentType(APPLICATION_JSON_VALUE);
		new ObjectMapper().writeValue(response.getOutputStream(), tokens);
	}
}
