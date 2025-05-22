package com.itesoft.registree.dao.jpa;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "reg_component")
public class ComponentEntity {
  @Id
  @Column(nullable = false)
  private String id;

  @ManyToOne
  @JoinColumn(name = "registry_name", updatable = false)
  private RegistryEntity registry;

  @Column(name = "group_name")
  private String group;

  @Column(nullable = false)
  private String name;

  @Column
  private String version;

  @Column(name = "creation_date")
  private OffsetDateTime creationDate;

  @Column(name = "update_date")
  private OffsetDateTime updateDate;

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
