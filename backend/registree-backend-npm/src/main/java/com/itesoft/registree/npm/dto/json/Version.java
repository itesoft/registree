package com.itesoft.registree.npm.dto.json;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Version {
  private String id;
  private String name;
  private String version;
  private String description;
  private VersionDist dist;
  private String resolved;
  private String from;
  @JsonAnyGetter
  @JsonAnySetter
  private final Map<String, Object> properties = new LinkedHashMap<>();

  @JsonProperty("_id")
  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public VersionDist getDist() {
    return dist;
  }

  public void setDist(final VersionDist dist) {
    this.dist = dist;
  }

  @JsonProperty("_resolved")
  public String getResolved() {
    return resolved;
  }

  public void setResolved(final String resolved) {
    this.resolved = resolved;
  }

  @JsonProperty("_from")
  public String getFrom() {
    return from;
  }

  public void setFrom(final String from) {
    this.from = from;
  }
}
