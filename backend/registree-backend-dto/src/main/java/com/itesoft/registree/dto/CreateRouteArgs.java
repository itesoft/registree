package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

public class CreateRouteArgs {
  public static class Builder {
    private String permissions;

    public Builder() {
      // empty default constructor
    }

    public Builder permissions(final String permissions) {
      this.permissions = permissions;
      return this;
    }

    public CreateRouteArgs build() {
      return new CreateRouteArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotEmpty
  private String permissions;

  public CreateRouteArgs() {
  }

  public CreateRouteArgs(final Builder builder) {
    this.permissions = builder.permissions;
  }

  public String getPermissions() {
    return permissions;
  }

  public void setPermissions(final String permissions) {
    this.permissions = permissions;
  }
}
