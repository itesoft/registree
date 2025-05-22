package com.itesoft.registree.dto;

import java.time.OffsetDateTime;

public class Component {
  private String id;
  private String registryName;
  private String registryFormat;
  private String group;
  private String name;
  private String version;
  private OffsetDateTime creationDate;
  private OffsetDateTime updateDate;

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

  public String getRegistryFormat() {
    return registryFormat;
  }

  public void setRegistryFormat(final String registryFormat) {
    this.registryFormat = registryFormat;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(final String group) {
    this.group = group;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(final String version) {
    this.version = version;
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
}
