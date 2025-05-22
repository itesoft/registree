package com.itesoft.registree.dto;

public abstract class StorageCapableRegistry extends Registry {
  private String storagePath;

  public abstract boolean isDoStore();

  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(final String storagePath) {
    this.storagePath = storagePath;
  }
}
