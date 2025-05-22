package com.itesoft.registree.npm.dto.json;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestPackage {
  private String id;
  private String name;
  private String description;
  private Map<String, String> distTags;
  private Map<String, Version> versions;
  private Map<String, Attachment> attachments;

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
  public Map<String, String> getDistTags() {
    return distTags;
  }

  public void setDistTags(final Map<String, String> distTags) {
    this.distTags = distTags;
  }

  public Map<String, Version> getVersions() {
    return versions;
  }

  public void setVersions(final Map<String, Version> versions) {
    this.versions = versions;
  }

  @JsonProperty("_attachments")
  public Map<String, Attachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(final Map<String, Attachment> attachments) {
    this.attachments = attachments;
  }
}
