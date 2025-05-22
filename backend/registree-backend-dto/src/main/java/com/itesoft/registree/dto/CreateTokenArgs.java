package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

import org.springframework.validation.annotation.Validated;

@Validated
public class CreateTokenArgs {
  public static class Builder {
    private String username;
    private String password;

    public Builder() {
      // empty default constructor
    }

    public Builder username(final String username) {
      this.username = username;
      return this;
    }

    public Builder password(final String password) {
      this.password = password;
      return this;
    }

    public CreateTokenArgs build() {
      return new CreateTokenArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotEmpty
  private String username;
  private String password;

  public CreateTokenArgs() {
  }

  public CreateTokenArgs(final Builder builder) {
    this.username = builder.username;
    this.password = builder.password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }
}
