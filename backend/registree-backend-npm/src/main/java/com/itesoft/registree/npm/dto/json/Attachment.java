package com.itesoft.registree.npm.dto.json;

public class Attachment {
  private String contentType;
  private String data;
  private long length;

  public String getContentType() {
    return contentType;
  }

  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  public String getData() {
    return data;
  }

  public void setData(final String data) {
    this.data = data;
  }

  public long getLength() {
    return length;
  }

  public void setLength(final long length) {
    this.length = length;
  }
}
