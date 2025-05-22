package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

public class UpdateComponentArgs {
  public static class Builder {
    private String group;
    private String name;
    private String version;

    public Builder() {
      // empty default constructor
    }

    public Builder group(final String group) {
      this.group = group;
      return this;
    }

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder version(final String version) {
      this.version = version;
      return this;
    }

    public UpdateComponentArgs build() {
      return new UpdateComponentArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private String group;
  @NotEmpty
  private String name;
  @NotEmpty
  private String version;

  public UpdateComponentArgs() {
  }

  public UpdateComponentArgs(final Builder builder) {
    this.group = builder.group;
    this.name = builder.name;
    this.version = builder.version;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(final String group) {
    this.group = group;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
  }
}
