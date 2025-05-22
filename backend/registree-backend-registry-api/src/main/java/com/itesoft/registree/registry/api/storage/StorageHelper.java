package com.itesoft.registree.registry.api.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.itesoft.registree.configuration.RegistreeDataConfiguration;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.StorageCapableRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StorageHelper {
  @Autowired
  private RegistreeDataConfiguration registreeDataConfiguration;

  public boolean getDoStore(final Registry registry) {
    final StorageCapableRegistry storageCapableRegistry = getStorageCapableRegistry(registry);
    return storageCapableRegistry.isDoStore();
  }

  public String getStoragePath(final Registry registry) {
    final StorageCapableRegistry storageCapableRegistry = getStorageCapableRegistry(registry);
    if (!storageCapableRegistry.isDoStore()) {
      throw new IllegalStateException("Cannot get storage path when not storing");
    }
    final Path path = Paths.get(storageCapableRegistry.getStoragePath());
    if (path.isAbsolute()) {
      return storageCapableRegistry.getStoragePath();
    } else {
      return String.format("%s/%s",
                           registreeDataConfiguration.getRegistriesPath(),
                           storageCapableRegistry.getStoragePath());
    }
  }

  private StorageCapableRegistry getStorageCapableRegistry(final Registry registry) {
    if (registry instanceof final StorageCapableRegistry storageCapableRegistry) {
      return storageCapableRegistry;
    }
    throw new IllegalArgumentException("Unexpected registry type");
  }
}
