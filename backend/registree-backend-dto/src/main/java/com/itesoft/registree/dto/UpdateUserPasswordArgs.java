package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

public class UpdateUserPasswordArgs {
  public static class Builder {
    private String newPassword;

    public Builder() {
      // empty default constructor
    }

    public Builder newPassword(final String newPassword) {
      this.newPassword = newPassword;
      return this;
    }

    public UpdateUserPasswordArgs build() {
      return new UpdateUserPasswordArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotEmpty
  private String newPassword;

  public UpdateUserPasswordArgs() {
  }

  public UpdateUserPasswordArgs(final Builder builder) {
    this.newPassword = builder.newPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(final String newPassword) {
    this.newPassword = newPassword;
  }
}
