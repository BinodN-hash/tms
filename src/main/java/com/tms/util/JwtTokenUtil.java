package com.tms.util;

import com.tms.exception.ApplicationException;
import com.tms.properties.SetUpProperties;
import com.tms.service.CustomUserDetails;
import com.tms.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class JwtTokenUtil implements Serializable {


	private static SetUpProperties setup = SetUpProperties.getInstance();

	@Autowired
	private CustomUserDetailsService userService;


	// retrieve username from jwt token
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	// retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	// for retrieveing any information from token we will need the secret key
	private Claims getAllClaimsFromToken(String token) {
		Claims claims = null;
		try {
			claims = Jwts.parser().setSigningKey(setup.getProperty("jwt.secret")).parseClaimsJws(token).getBody();
		}catch(Exception e) {
			e.printStackTrace();
			ApplicationHelper.sendError(setup.getProperty("settings.message.rest.token.error"), SC_UNAUTHORIZED);
			throw new ApplicationException(e.getMessage());
		}
		
		return claims;
	}

	// check if the token has expired
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	// generate Access token for user
	public String generateAccessToken(String username, int tokenValidity) {
		Map<String, Object> claims = new HashMap<>();

		String accessToken = doGenerateToken(claims, username, tokenValidity);

		userService.updateAccessToken(accessToken, username);
		return accessToken;
	}

	// generate Refresh token for user
	public String generateRefreshToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		
		String refreshToken = doGenerateToken(claims, username, setup.getIntProperty("jwt.refresh.token.validity"));
		
		userService.updateRefreshToken(refreshToken, username);
		return refreshToken;
	}

	// while creating the token -
	// 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
	// 2. Sign the JWT using the HS512 algorithm and secret key.
	// 3. According to JWS Compact
	// Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
	// compaction of the JWT to a URL-safe string
	private String doGenerateToken(Map<String, Object> claims, String subject, int tokenValidity) {

		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + tokenValidity * 60 * 1000))
				.signWith(SignatureAlgorithm.HS512, setup.getProperty("jwt.secret")).compact();
	}

	// validate token
	public Boolean validateToken(String token, CustomUserDetails userDetails) {
		final String username = getUsernameFromToken(token);

		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)
				&& token.equals(userDetails.getUser().getAccessToken()));
	}

	// validate token
	public Boolean validateRefreshToken(String token, CustomUserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)
				&& token.equals(userDetails.getUser().getRefreshToken()));

	}
}
