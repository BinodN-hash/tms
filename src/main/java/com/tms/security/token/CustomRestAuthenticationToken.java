package com.tms.security.token;

import com.tms.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomRestAuthenticationToken extends UsernamePasswordAuthenticationToken{

	private static final long serialVersionUID = 6429406367796298095L;

	public CustomRestAuthenticationToken(Object principal, Object credentials,
                                         Collection<? extends GrantedAuthority> authorities, User user) {
		super(principal, credentials, authorities);
		setDetails(user);
	}
	
	public CustomRestAuthenticationToken(Object principal, Object credentials) {
		super(principal, credentials);
	}

}
