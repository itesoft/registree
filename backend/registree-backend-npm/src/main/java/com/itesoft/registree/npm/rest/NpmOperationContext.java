package com.itesoft.registree.npm.rest;

import java.util.Map;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.npm.config.NpmRegistries;

public class NpmOperationContext {
  private final Map<String, NpmPackageManager> packageManagers;
  private final NpmRegistries npmRegistries;
  private final Registry registry;

  public NpmOperationContext(final Map<String, NpmPackageManager> packageManagers,
                             final NpmRegistries npmRegistries,
                             final Registry registry) {
    this.packageManagers = packageManagers;
    this.npmRegistries = npmRegistries;
    this.registry = registry;
  }

  public Registry getRegistry() {
    return registry;
  }

  public NpmOperationContext createSubContext(final String registryName) {
    for (final Registry registry : npmRegistries.getRegistries()) {
      if (registryName.equals(registry.getName())) {
        return new NpmOperationContext(packageManagers,
                                       npmRegistries,
                                       registry);
      }
    }
    throw new IllegalArgumentException(String.format("Cannot find registry with name %s", registryName));
  }

  public NpmPackageManager getPackageManager() {
    return packageManagers.get(registry.getType());
  }
}
