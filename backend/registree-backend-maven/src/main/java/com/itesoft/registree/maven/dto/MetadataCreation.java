package com.itesoft.registree.maven.dto;

import java.io.OutputStream;
import java.nio.file.Path;

public class MetadataCreation implements FileCreation {
  public static class Builder {
    private Path metadataPath;
    private Path tempMetadataPath;
    private OutputStream outputStream;

    public Builder() {
      // empty default constructor
    }

    public Builder metadataPath(final Path metadataPath) {
      this.metadataPath = metadataPath;
      return this;
    }

    public Builder tempMetadataPath(final Path tempMetadataPath) {
      this.tempMetadataPath = tempMetadataPath;
      return this;
    }

    public Builder outputStream(final OutputStream outputStream) {
      this.outputStream = outputStream;
      return this;
    }

    public MetadataCreation build() {
      return new MetadataCreation(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Path metadataPath;
  private Path tempMetadataPath;
  private OutputStream outputStream;

  public MetadataCreation() {
  }

  public MetadataCreation(final Builder builder) {
    this.metadataPath = builder.metadataPath;
    this.tempMetadataPath = builder.tempMetadataPath;
    this.outputStream = builder.outputStream;
  }

  public Path getMetadataPath() {
    return metadataPath;
  }

  public void setMetadataPath(final Path metadataPath) {
    this.metadataPath = metadataPath;
  }

  public Path getTempMetadataPath() {
    return tempMetadataPath;
  }

  public void setTempMetadataPath(final Path tempMetadataPath) {
    this.tempMetadataPath = tempMetadataPath;
  }

  @Override
  public OutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }
}
