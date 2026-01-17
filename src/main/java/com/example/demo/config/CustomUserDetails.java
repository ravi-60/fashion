package com.example.demo.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.model.User;

public class CustomUserDetails implements UserDetails {

    private final User user;
    
//    private SecurityExpressionRoot sp; // for ROLE_

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    public String getEmail() {
		return user.getEmail();
	}

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Long getUserId() {
        return user.getUserId();
    }

    public User.Role getRole() {
        return user.getRole();
    }

}