package com.tms.security.token;

import com.tms.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomWebAuthenticationToken extends UsernamePasswordAuthenticationToken{



	public CustomWebAuthenticationToken(Object principal, Object credentials,
                                        Collection<? extends GrantedAuthority> authorities, User user) {
		super(principal, credentials, authorities);
		setDetails(user);
	}
	
	public CustomWebAuthenticationToken(Object principal, Object credentials) {
		super(principal, credentials);
	}


}
