package com.felipe.trip_planner_user_service.security;

import com.felipe.trip_planner_user_service.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

  private final User user;

  public UserPrincipal(User user) {
    this.user = user;
  }

  public User getUser() {
    return this.user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String getPassword() {
    return this.user.getPassword();
  }

  @Override
  public String getUsername() {
    return this.user.getEmail();
  }
}
