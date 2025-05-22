package com.itesoft.registree.maven.dto;

import java.nio.file.Path;

public class MavenFile {
  public static class Builder {
    private Path path;
    private String contentType;

    public Builder() {
      // empty default constructor
    }

    public Builder path(final Path path) {
      this.path = path;
      return this;
    }

    public Builder contentType(final String contentType) {
      this.contentType = contentType;
      return this;
    }

    public MavenFile build() {
      return new MavenFile(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Path path;
  private String contentType;

  public MavenFile() {
  }

  public MavenFile(final Builder builder) {
    this.path = builder.path;
    this.contentType = builder.contentType;
  }

  public Path getPath() {
    return path;
  }

  public void setPath(final Path path) {
    this.path = path;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }
}
