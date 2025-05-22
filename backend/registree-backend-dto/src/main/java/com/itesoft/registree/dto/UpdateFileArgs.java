package com.itesoft.registree.dto;

import jakarta.validation.constraints.NotEmpty;

public class UpdateFileArgs {
  public static class Builder {
    private String path;
    private String contentType;

    public Builder() {
      // empty default constructor
    }

    public Builder path(final String path) {
      this.path = path;
      return this;
    }

    public Builder contentType(final String contentType) {
      this.contentType = contentType;
      return this;
    }

    public UpdateFileArgs build() {
      return new UpdateFileArgs(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  @NotEmpty
  private String path;
  @NotEmpty
  private String contentType;

  public UpdateFileArgs() {
  }

  public UpdateFileArgs(final Builder builder) {
    this.path = builder.path;
    this.contentType = builder.contentType;
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
