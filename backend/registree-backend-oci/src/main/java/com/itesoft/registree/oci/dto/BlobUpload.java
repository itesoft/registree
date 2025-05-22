package com.itesoft.registree.oci.dto;

import java.io.OutputStream;

public class BlobUpload {
  private final String name;
  private final String uuid;
  private long length;
  private final OutputStream outputStream;
  private long lastUpdate;

  public BlobUpload(final String name,
                    final String uuid,
                    final OutputStream outputStream) {
    this.name = name;
    this.uuid = uuid;
    this.outputStream = outputStream;
    this.lastUpdate = System.currentTimeMillis();
  }

  public String getName() {
    return name;
  }

  public String getUuid() {
    return uuid;
  }

  public long getLength() {
    return length;
  }

  public void incrementLength(final long bytes) {
    length += bytes;
  }

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public long getOffset() {
    if (length == 0) {
      return 0;
    }
    return length - 1;
  }

  public long getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(final long timestamp) {
    lastUpdate = timestamp;
  }
}
