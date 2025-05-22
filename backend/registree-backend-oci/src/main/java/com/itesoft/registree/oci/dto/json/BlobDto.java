package com.itesoft.registree.oci.dto.json;

public class BlobDto {
  private String mediaType;
  private long size;
  private String digest;

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(final String mediaType) {
    this.mediaType = mediaType;
  }

  public long getSize() {
    return size;
  }

  public void setSize(final long size) {
    this.size = size;
  }

  public String getDigest() {
    return digest;
  }

  public void setDigest(final String digest) {
    this.digest = digest;
  }
}
