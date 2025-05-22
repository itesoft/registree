package com.itesoft.registree.oci.dto;

import java.nio.file.Path;

public class Blob {
  private final String digest;
  private final String contentType;
  private final long contentLength;
  private final Path path;

  public Blob(final String digest,
              final String contentType,
              final long contentLength,
              final Path path) {
    this.digest = digest;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.path = path;
  }

  public String getDigest() {
    return digest;
  }

  public String getContentType() {
    return contentType;
  }

  public long getContentLength() {
    return contentLength;
  }

  public Path getPath() {
    return path;
  }
}
