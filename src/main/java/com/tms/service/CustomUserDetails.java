package com.tms.service;

import com.tms.model.RoleType;
import com.tms.model.Permission;
import com.tms.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;



public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 4403534607230678972L;

	private final User user;
	
	public CustomUserDetails(User user) {
		this.user = user;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return getAuthorities(Arrays.asList(user));
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public final User getUser() {
        return user;
    }
	
	public String getEmail() {
		return user.getEmail();
	}
	

	private Collection<? extends GrantedAuthority> getAuthorities(final Collection<User> users){
		return getGrantedAuthorities(getPermissions(users));
	}
	
	private List<String> getPermissions(final Collection<User> users){
		final List<String> permissions = new ArrayList<>();
		final List<Permission> collection = new ArrayList<>();
		for(final User user : users) {
			if(user.getRole().getType().equals(RoleType.SUPER_ADMIN)) {
				permissions.add("SUPER_ADMIN");
			}
			collection.addAll(user.getPermissions());
		}
		
		for(final Permission item : collection) {
			permissions.add(item.getCode());
		}
		return permissions;
	}
	
	private List<GrantedAuthority> getGrantedAuthorities(final List<String> permissions){
		final List<GrantedAuthority> authorities = new ArrayList<>();
		for(final String permission : permissions) {
			authorities.add(new SimpleGrantedAuthority(permission));
		}
		return authorities;
	}

}
