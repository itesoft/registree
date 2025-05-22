package com.itesoft.registree.raw.dto;

import java.io.OutputStream;
import java.nio.file.Path;

public class RawFileCreation {
  public static class Builder {
    private Path filePath;
    private Path tempFilePath;
    private OutputStream outputStream;

    public Builder() {
      // empty default constructor
    }

    public Builder filePath(final Path filePath) {
      this.filePath = filePath;
      return this;
    }

    public Builder tempFilePath(final Path tempFilePath) {
      this.tempFilePath = tempFilePath;
      return this;
    }

    public Builder outputStream(final OutputStream outputStream) {
      this.outputStream = outputStream;
      return this;
    }

    public RawFileCreation build() {
      return new RawFileCreation(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Path filePath;
  private Path tempFilePath;
  private OutputStream outputStream;

  public RawFileCreation() {
  }

  public RawFileCreation(final Builder builder) {
    this.filePath = builder.filePath;
    this.tempFilePath = builder.tempFilePath;
    this.outputStream = builder.outputStream;
  }

  public Path getFilePath() {
    return filePath;
  }

  public void setFilePath(final Path filePath) {
    this.filePath = filePath;
  }

  public Path getTempFilePath() {
    return tempFilePath;
  }

  public void setTempFilePath(final Path tempFilePath) {
    this.tempFilePath = tempFilePath;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }
}
