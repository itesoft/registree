package com.itesoft.registree.dao.jpa;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "reg_file")
public class FileEntity {
  @Id
  @Column(nullable = false)
  private String id;

  @ManyToOne
  @JoinColumn(name = "registry_name", nullable = false, updatable = false)
  private RegistryEntity registry;

  @ManyToOne
  @JoinColumn(name = "component_id", updatable = false)
  private ComponentEntity component;

  @Column(nullable = false)
  private String path;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "creation_date")
  private OffsetDateTime creationDate;

  @Column(name = "update_date")
  private OffsetDateTime updateDate;

  @Column
  private String uploader;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public RegistryEntity getRegistry() {
    return registry;
  }

  public void setRegistry(final RegistryEntity registry) {
    this.registry = registry;
  }

  public ComponentEntity getComponent() {
    return component;
  }

  public void setComponent(final ComponentEntity component) {
    this.component = component;
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
