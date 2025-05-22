package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public class Route {
  @NotEmpty
  private UserIdentifier userIdentifier;

  @NotEmpty
  private String path;

  @NotNull
  private String permissions;

  public UserIdentifier getUserIdentifier() {
    return userIdentifier;
  }

  public void setUserIdentifier(final UserIdentifier userIdentifier) {
    this.userIdentifier = userIdentifier;
  }

  public String getPath() {
    return path;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public String getPermissions() {
    return permissions;
  }

  public void setPermissions(final String permissions) {
    this.permissions = permissions;
  }
}
