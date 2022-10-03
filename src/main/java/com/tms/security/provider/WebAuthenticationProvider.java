package com.tms.security.provider;


import com.tms.model.Credentials;
import com.tms.properties.SetUpProperties;
import com.tms.security.token.CustomWebAuthenticationToken;
import com.tms.service.CustomUserDetails;
import com.tms.service.CustomUserDetailsService;
import com.tms.service.WrongAttemptService;
import com.tms.util.ApplicationHelper;
import com.tms.util.Consts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebAuthenticationProvider implements AuthenticationProvider {

	private static SetUpProperties setup = SetUpProperties.getInstance();
	
	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private WrongAttemptService wrongAttemptService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = String.valueOf(authentication.getCredentials());
		String errorMsg = Consts.EMPTY;
		
		CustomUserDetails userDetails = userDetailsService.loadUserByUsername(username);
		
		if(setup.getProperty("web.member.login.disable").equalsIgnoreCase("true") && !userDetails.getUser().isSuperAdmin()) {
			throw new BadCredentialsException(setup.getProperty("settings.message.web.login.disable"));
		}


		if (Boolean.TRUE.equals(ApplicationHelper.isBlocked(userDetails.getUser().getPasswordBlockedUntil()))) {
			errorMsg = setup.getProperty("settings.message.blocked.credential");
			throw new BadCredentialsException(errorMsg);
		}

		if (!passwordEncoder.matches(password, userDetails.getPassword())) {
			// Check if maxTries has been reached
			wrongAttemptService.record(userDetails.getUser(), Credentials.LOGIN_PASSWORD);
			int wrongAttempts = wrongAttemptService.count(ApplicationHelper.wrongAttemptsLimit(), userDetails.getUser(),
					Credentials.LOGIN_PASSWORD);
			log.info("Wrong Attempt : " + wrongAttempts);
			int maxTries = setup.getIntProperty("invalid.login.maxTries");
			if (wrongAttempts >= maxTries) {
				// Set the block time and clear previous attempts
				userDetails.getUser().setPasswordBlockedUntil(ApplicationHelper.blockPasswordUntil(setup.getIntProperty("block.credentials.until")));
				wrongAttemptService.clear(userDetails.getUser(), Credentials.LOGIN_PASSWORD);
				userDetailsService.updateUser(userDetails.getUser());
				
				throw new BadCredentialsException(setup.getProperty("settings.message.blocked.credential"));
			}
			
			errorMsg = String.format(setup.getProperty("settings.message.login.invalid"),maxTries-wrongAttempts);
			throw new BadCredentialsException(errorMsg);
		}
		// Everything Ok - remove any previous wrong attempts
		wrongAttemptService.clear(userDetails.getUser(), Credentials.LOGIN_PASSWORD);

		return new CustomWebAuthenticationToken(username, password,  userDetails.getAuthorities(),userDetails.getUser());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return CustomWebAuthenticationToken.class.equals(authentication);
	}

}
