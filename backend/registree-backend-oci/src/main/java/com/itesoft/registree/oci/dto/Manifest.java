package com.itesoft.registree.oci.dto;

public class Manifest {
  private final String digest;
  private final String contentType;
  private final long contentLength;
  private final byte[] data;

  public Manifest(final String digest,
                  final String contentType,
                  final long contentLength,
                  final byte[] data) {
    this.digest = digest;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.data = data;
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

  public byte[] getData() {
    return data;
  }
}
