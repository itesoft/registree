package com.itesoft.registree.maven.dto.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "metadata")
public class MavenMetadata {
  private String groupId;
  private String artifactId;
  private MavenMetadataVersioning versioning;

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(final String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(final String artifactId) {
    this.artifactId = artifactId;
  }

  public MavenMetadataVersioning getVersioning() {
    return versioning;
  }

  public void setVersioning(final MavenMetadataVersioning versioning) {
    this.versioning = versioning;
  }
}
