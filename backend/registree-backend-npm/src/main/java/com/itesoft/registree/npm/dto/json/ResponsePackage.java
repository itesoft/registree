package com.itesoft.registree.npm.dto.json;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponsePackage {
  private String id;
  private String name;
  private String description;
  private LinkedHashMap<String, String> distTags;
  private LinkedHashMap<String, Version> versions;
  private LinkedHashMap<String, OffsetDateTime> time;

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

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @JsonProperty("dist-tags")
  public LinkedHashMap<String, String> getDistTags() {
    return distTags;
  }

  public void setDistTags(final LinkedHashMap<String, String> distTags) {
    this.distTags = distTags;
  }

  public LinkedHashMap<String, Version> getVersions() {
    return versions;
  }

  public void setVersions(final LinkedHashMap<String, Version> versions) {
    this.versions = versions;
  }

  public LinkedHashMap<String, OffsetDateTime> getTime() {
    return time;
  }

  public void setTime(final LinkedHashMap<String, OffsetDateTime> time) {
    this.time = time;
  }
}
