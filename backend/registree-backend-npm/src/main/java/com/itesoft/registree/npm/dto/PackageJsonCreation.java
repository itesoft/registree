package com.itesoft.registree.npm.dto;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

public class PackageJsonCreation {
  public static class Builder {
    private Path packageJsonPath;
    private OutputStream outputStream;
    private Lock lock;

    public Builder() {
      // empty default constructor
    }

    public Builder packageJsonPath(final Path packageJsonPath) {
      this.packageJsonPath = packageJsonPath;
      return this;
    }

    public Builder outputStream(final OutputStream outputStream) {
      this.outputStream = outputStream;
      return this;
    }

    public Builder lock(final Lock lock) {
      this.lock = lock;
      return this;
    }

    public PackageJsonCreation build() {
      return new PackageJsonCreation(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Path packageJsonPath;
  private OutputStream outputStream;
  private Lock lock;

  public PackageJsonCreation() {
  }

  public PackageJsonCreation(final Builder builder) {
    this.packageJsonPath = builder.packageJsonPath;
    this.outputStream = builder.outputStream;
    this.lock = builder.lock;
  }

  public Path getPackageJsonPath() {
    return packageJsonPath;
  }

  public void setPackageJsonPath(final Path packageJsonPath) {
    this.packageJsonPath = packageJsonPath;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public void setOutputStream(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public Lock getLock() {
    return lock;
  }

  public void setLock(final Lock lock) {
    this.lock = lock;
  }
}
