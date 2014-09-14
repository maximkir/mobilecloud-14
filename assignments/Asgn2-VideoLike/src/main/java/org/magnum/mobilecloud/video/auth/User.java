package org.magnum.mobilecloud.video.auth;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

public class User implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = -970470193332162610L;

	public static UserDetails create(String username, String password, String...authorities) {
		return new User(username, password, authorities);
	}
	
	private final Collection<GrantedAuthority> authorities;
	private final String passw;
	private final String username;

	@SuppressWarnings("unchecked")
	private User(String username, String password) {
		this(username, password, Collections.EMPTY_LIST);
	}

	private User(String username, String password, String...authorities) {
		this.username = username;
		this.passw = password;
		this.authorities = AuthorityUtils.createAuthorityList(authorities);
	}

	private User(String username, String password, Collection<GrantedAuthority> authorities) {
		super();
		this.username = username;
		this.passw = password;
		this.authorities = authorities;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public String getPassword() {
		return passw;
	}

	public String getUsername() {
		return username;
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

}

