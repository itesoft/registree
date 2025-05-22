package com.itesoft.registree.npm.dto.json;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class VersionDist {
  private String integrity;
  private String tarball;
  @JsonAnyGetter
  @JsonAnySetter
  private final Map<String, Object> properties = new LinkedHashMap<>();

  public String getIntegrity() {
    return integrity;
  }

  public void setIntegrity(final String integrity) {
    this.integrity = integrity;
  }

  public String getTarball() {
    return tarball;
  }

  public void setTarball(final String tarball) {
    this.tarball = tarball;
  }
}
