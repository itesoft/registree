package com.itesoft.registree.dto;

public class UpdateRegistryArgs {
  public static class Builder {
    private String configuration;

    public Builder() {
      // empty default constructor
    }

    public Builder configuration(final String configuration) {
      this.configuration = configuration;
      return this;
    }

    public UpdateRegistryArgs build() {
      return new UpdateRegistryArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private String configuration;

  public UpdateRegistryArgs() {
  }

  public UpdateRegistryArgs(final Builder builder) {
    this.configuration = builder.configuration;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(final String configuration) {
    this.configuration = configuration;
  }
}
