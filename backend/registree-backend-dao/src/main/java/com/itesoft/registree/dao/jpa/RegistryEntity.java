package com.itesoft.registree.dao.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity(name = "reg_registry")
public class RegistryEntity {
  @Id
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String format;

  @Column(nullable = false)
  private String type;

  @Column(name = "configuration", columnDefinition = "jsonb")
  @JdbcTypeCode(SqlTypes.JSON)
  private String configuration;

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getFormat() {
    return format;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(final String configuration) {
    this.configuration = configuration;
  }
}
