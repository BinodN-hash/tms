package com.tms.security.filter;

import com.tms.exception.ApplicationException;
import com.tms.exception.InvalidCredentialException;
import com.tms.properties.SetUpProperties;
import com.tms.service.CustomUserDetails;
import com.tms.service.CustomUserDetailsService;
import com.tms.util.ApplicationHelper;
import com.tms.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired

	private static SetUpProperties setup = SetUpProperties.getInstance();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			if (request.getServletPath().equals("/rest/access/login")
					|| request.getServletPath().equals("/rest/access/token/refresh")) {
				filterChain.doFilter(request, response);
			} else {
				final String requestTokenHeader = request.getHeader("ACCESS-TOKEN");

				String username = null;
				String jwtToken = null;
				// JWT Token is in the form "Bearer token". Remove Bearer word and get
				// only the Token
				if (requestTokenHeader != null) {
					jwtToken = requestTokenHeader;
					try {
						username = jwtTokenUtil.getUsernameFromToken(jwtToken);
					} catch (IllegalArgumentException e) {
						logger.warn("Unable to get JWT Token");
						 ApplicationHelper.sendError(setup.getProperty("settings.message.rest.token.error"), SC_UNAUTHORIZED);
						throw new ApplicationException(setup.getProperty("settings.message.rest.token.error"));
					} catch (ExpiredJwtException e) {
						ApplicationHelper.sendError(setup.getProperty("settings.message.rest.token.expired"), SC_UNAUTHORIZED);
						throw new InvalidCredentialException(setup.getProperty("settings.message.rest.token.expired"));
					}
				} else {
					logger.error("JWT Token does not begin with Bearer String");
				}

				// Once we get the token validate it.
				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

					CustomUserDetails userDetails = userDetailsService.loadRestUserByUsername(username);

					// if token is valid configure Spring Security to manually set
					// authentication
					if (!jwtTokenUtil.validateToken(jwtToken, userDetails)) {
						ApplicationHelper.sendError(setup.getProperty("settings.message.rest.token.error"), SC_UNAUTHORIZED);
						throw new ApplicationException(setup.getProperty("settings.message.rest.token.error"));
					}
					UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
							userDetails, null, userDetails.getAuthorities());
					usernamePasswordAuthenticationToken
							.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					// After setting the Authentication in the context, we specify
					// that the current user is authenticated. So it passes the
					// Spring Security Configurations successfully.
					SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				}
				filterChain.doFilter(request, response);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ApplicationException(e.getMessage());
		}
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return !Objects.nonNull(request.getHeader("ACCESS-TOKEN"));
	}


}
