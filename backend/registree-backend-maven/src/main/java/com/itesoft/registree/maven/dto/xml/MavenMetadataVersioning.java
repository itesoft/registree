package com.itesoft.registree.maven.dto.xml;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class MavenMetadataVersioning {
  private String latest;
  private String release;
  @JacksonXmlElementWrapper(localName = "versions")
  @JacksonXmlProperty(localName = "version")
  private List<String> versions;
  private String lastUpdated;

  public String getLatest() {
    return latest;
  }

  public void setLatest(final String latest) {
    this.latest = latest;
  }

  public String getRelease() {
    return release;
  }

  public void setRelease(final String release) {
    this.release = release;
  }

  public List<String> getVersions() {
    return versions;
  }

  public void setVersions(final List<String> versions) {
    this.versions = versions;
  }

  public String getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(final String lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
}
