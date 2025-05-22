package com.itesoft.registree.raw.rest;

import java.util.Map;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.raw.config.RawRegistries;

public class RawOperationContext {
  private final Map<String, RawFileManager> fileManagers;
  private final RawRegistries rawRegistries;
  private final Registry registry;

  public RawOperationContext(final Map<String, RawFileManager> fileManagers,
                             final RawRegistries rawRegistries,
                             final Registry registry) {
    this.fileManagers = fileManagers;
    this.rawRegistries = rawRegistries;
    this.registry = registry;
  }

  public Registry getRegistry() {
    return registry;
  }

  public RawOperationContext createSubContext(final String registryName) {
    for (final Registry registry : rawRegistries.getRegistries()) {
      if (registryName.equals(registry.getName())) {
        return new RawOperationContext(fileManagers,
                                       rawRegistries,
                                       registry);
      }
    }
    throw new IllegalArgumentException(String.format("Cannot find registry with name %s", registryName));
  }

  public RawFileManager getFileManager() {
    return fileManagers.get(registry.getType());
  }
}
