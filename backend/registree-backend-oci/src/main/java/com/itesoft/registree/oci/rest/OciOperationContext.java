package com.itesoft.registree.oci.rest;

import java.util.Map;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.oci.config.OciRegistries;

public class OciOperationContext {
  private final Map<String, OciRegistryBlobManager> registryBlobManagers;
  private final Map<String, OciRegistryBlobUploadManager> registryBlobUploadManagers;
  private final Map<String, OciRegistryManifestManager> registryManifestManagers;
  private final Map<String, OciRegistryRepositoryManager> registryRepositoryManagers;
  private final OciRegistries ociRegistries;
  private final Registry registry;

  public OciOperationContext(final Map<String, OciRegistryBlobManager> registryBlobManagers,
                             final Map<String, OciRegistryBlobUploadManager> registryBlobUploadManagers,
                             final Map<String, OciRegistryManifestManager> registryManifestManagers,
                             final Map<String, OciRegistryRepositoryManager> registryRepositoryManagers,
                             final OciRegistries ociRregistries,
                             final Registry registry) {
    this.registryBlobManagers = registryBlobManagers;
    this.registryBlobUploadManagers = registryBlobUploadManagers;
    this.registryManifestManagers = registryManifestManagers;
    this.registryRepositoryManagers = registryRepositoryManagers;
    this.ociRegistries = ociRregistries;
    this.registry = registry;
  }

  public Registry getRegistry() {
    return registry;
  }

  public OciOperationContext createSubContext(final String registryName) {
    for (final Registry registry : ociRegistries.getRegistries()) {
      if (registryName.equals(registry.getName())) {
        return new OciOperationContext(registryBlobManagers,
                                       registryBlobUploadManagers,
                                       registryManifestManagers,
                                       registryRepositoryManagers,
                                       ociRegistries,
                                       registry);
      }
    }
    throw new IllegalArgumentException(String.format("Cannot find registry with name %s", registryName));
  }

  public OciRegistryBlobManager getRegistryBlobManager() {
    return registryBlobManagers.get(registry.getType());
  }

  public OciRegistryBlobUploadManager getRegistryBlobUploadManager() {
    return registryBlobUploadManagers.get(registry.getType());
  }

  public OciRegistryManifestManager getRegistryManifestManager() {
    return registryManifestManagers.get(registry.getType());
  }

  public OciRegistryRepositoryManager getRegistryRepositoryManager() {
    return registryRepositoryManagers.get(registry.getType());
  }

  @SuppressWarnings("unchecked")
  public <T> T getRegistryManager(final Class<T> registryManagerClass) {
    if (OciRegistryBlobManager.class.equals(registryManagerClass)) {
      return (T) getRegistryBlobManager();
    } else if (OciRegistryBlobUploadManager.class.equals(registryManagerClass)) {
      return (T) getRegistryBlobUploadManager();
    } else if (OciRegistryManifestManager.class.equals(registryManagerClass)) {
      return (T) getRegistryManifestManager();
    } else if (OciRegistryRepositoryManager.class.equals(registryManagerClass)) {
      return (T) getRegistryRepositoryManager();
    } else {
      throw new IllegalArgumentException(String.format("Unexpected registry manager class %s", registryManagerClass));
    }
  }
}
