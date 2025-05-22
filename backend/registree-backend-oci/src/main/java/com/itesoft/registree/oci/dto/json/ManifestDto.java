package com.itesoft.registree.oci.dto.json;

import java.util.List;

public class ManifestDto {
  private int schemaVersion;
  private String mediaType;
  private BlobDto config;
  private List<BlobDto> layers;

  public int getSchemaVersion() {
    return schemaVersion;
  }

  public void setSchemaVersion(final int schemaVersion) {
    this.schemaVersion = schemaVersion;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(final String mediaType) {
    this.mediaType = mediaType;
  }

  public BlobDto getConfig() {
    return config;
  }

  public void setConfig(final BlobDto config) {
    this.config = config;
  }

  public List<BlobDto> getLayers() {
    return layers;
  }

  public void setLayers(final List<BlobDto> layers) {
    this.layers = layers;
  }
}
