package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

public class CreateUserArgs {
  public static class Builder {
    private String username;
    private String password;
    private String firstName;
    private String lastName;

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

    public Builder firstName(final String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(final String lastName) {
      this.lastName = lastName;
      return this;
    }

    public CreateUserArgs build() {
      return new CreateUserArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotEmpty
  private String username;
  private String password;
  private String firstName;
  private String lastName;

  public CreateUserArgs() {
  }

  public CreateUserArgs(final Builder builder) {
    this.username = builder.username;
    this.password = builder.password;
    this.firstName = builder.firstName;
    this.lastName = builder.lastName;
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

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }
}
