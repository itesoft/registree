package com.itesoft.registree.security.auth;

public class AuthenticationResult {
  public static class Builder {
    private boolean authenticated;
    private RegistreeUserDetails userDetails;

    public Builder() {
      // empty default constructor
    }

    public Builder authenticated(final boolean authenticated) {
      this.authenticated = authenticated;
      return this;
    }

    public Builder userDetails(final RegistreeUserDetails userDetails) {
      this.userDetails = userDetails;
      return this;
    }

    public AuthenticationResult build() {
      return new AuthenticationResult(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private boolean authenticated;
  private RegistreeUserDetails userDetails;

  public AuthenticationResult() {
  }

  public AuthenticationResult(final Builder builder) {
    this.authenticated = builder.authenticated;
    this.userDetails = builder.userDetails;
  }

  public boolean isAuthenticated() {
    return authenticated;
  }

  public void setAuthenticated(final boolean authenticated) {
    this.authenticated = authenticated;
  }

  public RegistreeUserDetails getUserDetails() {
    return userDetails;
  }

  public void setUserDetails(final RegistreeUserDetails userDetails) {
    this.userDetails = userDetails;
  }
}
