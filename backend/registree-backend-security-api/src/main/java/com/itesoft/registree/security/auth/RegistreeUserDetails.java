package com.itesoft.registree.security.auth;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class RegistreeUserDetails implements UserDetails {
  public static class Builder {
    private Long id;
    private String username;

    public Builder() {
      // empty default constructor
    }

    public Builder id(final Long id) {
      this.id = id;
      return this;
    }

    public Builder username(final String username) {
      this.username = username;
      return this;
    }

    public RegistreeUserDetails build() {
      return new RegistreeUserDetails(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private static final long serialVersionUID = 1L;
  private static final String NOT_IMPLEMENTED = "Not implemented, yet";
  private final Long id;
  private final String username;

  public RegistreeUserDetails(final Builder builder) {
    this.id = builder.id;
    this.username = builder.username;
  }

  public Long getId() {
    return id;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean isAccountNonExpired() {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean isAccountNonLocked() {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean isCredentialsNonExpired() {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean isEnabled() {
    throw new UnsupportedOperationException(NOT_IMPLEMENTED);
  }
}
