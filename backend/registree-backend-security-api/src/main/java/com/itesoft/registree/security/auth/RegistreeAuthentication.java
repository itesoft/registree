package com.itesoft.registree.security.auth;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class RegistreeAuthentication implements Authentication {
  public static class Builder {
    private String name;
    private Collection<? extends GrantedAuthority> authorities;
    private Object credentials;
    private Object details;
    private Object principal;
    private boolean isAuthenticated;

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder authorities(final Collection<? extends GrantedAuthority> authorities) {
      this.authorities = authorities;
      return this;
    }

    public Builder credentials(final Object credentials) {
      this.credentials = credentials;
      return this;
    }

    public Builder details(final Object details) {
      this.details = details;
      return this;
    }

    public Builder principal(final Object principal) {
      this.principal = principal;
      return this;
    }

    public Builder isAuthenticated(final boolean isAuthenticated) {
      this.isAuthenticated = isAuthenticated;
      return this;
    }

    public RegistreeAuthentication build() {
      return new RegistreeAuthentication(this);
    }

  }

  public static Builder builder() {
    return new Builder();
  }

  private static final long serialVersionUID = 1L;

  private final String name;
  private final Collection<? extends GrantedAuthority> authorities;
  private final Object credentials;
  private final Object details;
  private final Object principal;
  private boolean isAuthenticated;

  public RegistreeAuthentication(final Builder builder) {
    this.name = builder.name;
    this.authorities = builder.authorities;
    this.credentials = builder.credentials;
    this.details = builder.details;
    this.principal = builder.principal;
    this.isAuthenticated = builder.isAuthenticated;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public Object getCredentials() {
    return credentials;
  }

  @Override
  public Object getDetails() {
    return details;
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public boolean isAuthenticated() {
    return isAuthenticated;
  }

  @Override
  public void setAuthenticated(final boolean isAuthenticated) throws IllegalArgumentException {
    this.isAuthenticated = isAuthenticated;
  }
}
