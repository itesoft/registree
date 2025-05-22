package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

public class CreateFileArgs {
  public static class Builder {
    private String registryName;
    private String componentId;
    private String path;
    private String contentType;

    public Builder() {
      // empty default constructor
    }

    public Builder registryName(final String registryName) {
      this.registryName = registryName;
      return this;
    }

    public Builder componentId(final String componentId) {
      this.componentId = componentId;
      return this;
    }

    public Builder path(final String path) {
      this.path = path;
      return this;
    }

    public Builder contentType(final String contentType) {
      this.contentType = contentType;
      return this;
    }

    public CreateFileArgs build() {
      return new CreateFileArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private String registryName;
  private String componentId;
  @NotEmpty
  private String path;
  @NotEmpty
  private String contentType;

  public CreateFileArgs() {
  }

  public CreateFileArgs(final Builder builder) {
    this.registryName = builder.registryName;
    this.componentId = builder.componentId;
    this.path = builder.path;
    this.contentType = builder.contentType;
  }

  public String getRegistryName() {
    return registryName;
  }

  public void setRegistryName(final String registryName) {
    this.registryName = registryName;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }
}
