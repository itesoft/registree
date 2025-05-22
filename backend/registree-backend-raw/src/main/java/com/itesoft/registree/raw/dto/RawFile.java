package com.itesoft.registree.raw.dto;

import java.nio.file.Path;

public class RawFile {
  public static class Builder {
    private Path path;

    public Builder() {
      // empty default constructor
    }

    public Builder path(final Path path) {
      this.path = path;
      return this;
    }

    public RawFile build() {
      return new RawFile(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Path path;

  public RawFile() {
  }

  public RawFile(final Builder builder) {
    this.path = builder.path;
  }

  public Path getPath() {
    return path;
  }

  public void setPath(final Path path) {
    this.path = path;
  }
}
