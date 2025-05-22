package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

public class CreateRegistryArgs {
  public static class Builder {
    private String name;
    private String format;
    private String type;
    private String configuration;

    public Builder() {
      // empty default constructor
    }

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder format(final String format) {
      this.format = format;
      return this;
    }

    public Builder type(final String type) {
      this.type = type;
      return this;
    }

    public Builder configuration(final String configuration) {
      this.configuration = configuration;
      return this;
    }

    public CreateRegistryArgs build() {
      return new CreateRegistryArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotEmpty
  private String name;
  @NotEmpty
  private String format;
  @NotEmpty
  private String type;
  private String configuration;

  public CreateRegistryArgs() {
  }

  public CreateRegistryArgs(final Builder builder) {
    this.name = builder.name;
    this.format = builder.format;
    this.type = builder.type;
    this.configuration = builder.configuration;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(final String configuration) {
    this.configuration = configuration;
  }
}
