package com.itesoft.registree.dto;

import java.time.OffsetDateTime;

public class File {
  private String id;
  private String registryName;
  private String componentId;
  private String path;
  private String contentType;
  private OffsetDateTime creationDate;
  private OffsetDateTime updateDate;
  private String uploader;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getRegistryName() {
    return registryName;
  }

  public void setRegistryName(final String registryName) {
    this.registryName = registryName;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(final String path) {
    this.path = path;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(final String contentType) {
    this.contentType = contentType;
  }

  public OffsetDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(final OffsetDateTime creationDate) {
    this.creationDate = creationDate;
  }

  public OffsetDateTime getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(final OffsetDateTime updateDate) {
    this.updateDate = updateDate;
  }

  public String getUploader() {
    return uploader;
  }

  public void setUploader(final String uploader) {
    this.uploader = uploader;
  }
}
