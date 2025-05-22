package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CreateComponentArgs {
  public static class Builder {
    private String registryName;
    private String group;
    private String name;
    private String version;

    public Builder() {
      // empty default constructor
    }

    public Builder registryName(final String registryName) {
      this.registryName = registryName;
      return this;
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

    public CreateComponentArgs build() {
      return new CreateComponentArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotNull
  private String registryName;
  private String group;
  @NotEmpty
  private String name;
  @NotEmpty
  private String version;

  public CreateComponentArgs() {
  }

  public CreateComponentArgs(final Builder builder) {
    this.registryName = builder.registryName;
    this.group = builder.group;
    this.name = builder.name;
    this.version = builder.version;
  }

  public String getRegistryName() {
    return registryName;
  }

  public void setRegistryName(final String registryName) {
    this.registryName = registryName;
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
