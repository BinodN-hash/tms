package com.tms.security.provider;


import com.tms.model.Credentials;
import com.tms.properties.SetUpProperties;
import com.tms.security.token.CustomRestAuthenticationToken;
import com.tms.service.CustomUserDetails;
import com.tms.service.CustomUserDetailsService;
import com.tms.service.WrongAttemptService;
import com.tms.util.ApplicationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@Component
public class RestAuthenticationProvider implements AuthenticationProvider {

	private static SetUpProperties setup = SetUpProperties.getInstance();
	
	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private WrongAttemptService wrongAttemptService;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = String.valueOf(authentication.getCredentials());
		CustomUserDetails userDetails = userDetailsService.loadRestUserByUsername(username);
		try {
			if (ApplicationHelper.isBlocked(userDetails.getUser().getPasswordBlockedUntil())) {
			throw new BadCredentialsException(setup.getProperty("settings.message.blocked.credential"));
			}
		
			if (!passwordEncoder.matches(password, userDetails.getPassword())) {
				// Check if maxTries has been reached
				wrongAttemptService.record(userDetails.getUser(), Credentials.LOGIN_PASSWORD);
				int wrongAttempts = wrongAttemptService.count(ApplicationHelper.wrongAttemptsLimit(),
						userDetails.getUser(), Credentials.LOGIN_PASSWORD);
				log.info("Wrong Attempt : " + wrongAttempts);
				int maxTries = setup.getIntProperty("invalid.login.maxTries");
				if (wrongAttempts >= maxTries) {
					// Set the block time and clear previous attempts
					userDetails.getUser().setPasswordBlockedUntil(
							ApplicationHelper.blockPasswordUntil(setup.getIntProperty("block.credentials.until")));
					wrongAttemptService.clear(userDetails.getUser(), Credentials.LOGIN_PASSWORD);
					userDetailsService.updateUser(userDetails.getUser());
					throw new BadCredentialsException(setup.getProperty("settings.message.blocked.credential"));
				}
				throw new BadCredentialsException(String.format(setup.getProperty("settings.message.login.invalid"),maxTries-wrongAttempts));
			}
		} catch (Exception e) {
			e.printStackTrace();
			ApplicationHelper.sendError(e.getMessage(), UNAUTHORIZED.value());
			throw new BadCredentialsException(e.getMessage());
		}
		// Everything Ok - remove any previous wrong attempts
		wrongAttemptService.clear(userDetails.getUser(), Credentials.LOGIN_PASSWORD);
		return new CustomRestAuthenticationToken(username, password, userDetails.getAuthorities(),userDetails.getUser());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return CustomRestAuthenticationToken.class.equals(authentication);
	}
}
