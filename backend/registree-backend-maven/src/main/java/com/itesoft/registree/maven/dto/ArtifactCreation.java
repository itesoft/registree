package com.itesoft.registree.maven.dto;

import java.io.OutputStream;
import java.nio.file.Path;

public class ArtifactCreation implements FileCreation {
  public static class Builder {
    private Path tarballPath;
    private Path tempTarballPath;
    private OutputStream outputStream;

    public Builder() {
      // empty default constructor
    }

    public Builder tarballPath(final Path tarballPath) {
      this.tarballPath = tarballPath;
      return this;
    }

    public Builder tempTarballPath(final Path tempTarballPath) {
      this.tempTarballPath = tempTarballPath;
      return this;
    }

    public Builder outputStream(final OutputStream outputStream) {
      this.outputStream = outputStream;
      return this;
    }

    public ArtifactCreation build() {
      return new ArtifactCreation(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Path tarballPath;
  private Path tempTarballPath;
  private OutputStream outputStream;

  public ArtifactCreation() {
  }

  public ArtifactCreation(final Builder builder) {
    this.tarballPath = builder.tarballPath;
    this.tempTarballPath = builder.tempTarballPath;
    this.outputStream = builder.outputStream;
  }

  public Path getTarballPath() {
    return tarballPath;
  }

  public void setTarballPath(final Path tarballPath) {
    this.tarballPath = tarballPath;
  }

  public Path getTempTarballPath() {
    return tempTarballPath;
  }

  public void setTempTarballPath(final Path tempTarballPath) {
    this.tempTarballPath = tempTarballPath;
  }

  @Override
  public OutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }
}
