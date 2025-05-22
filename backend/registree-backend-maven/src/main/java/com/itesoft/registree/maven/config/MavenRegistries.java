package com.itesoft.registree.maven.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.itesoft.registree.dto.Registry;

import org.springframework.stereotype.Component;

@Component
public class MavenRegistries {
  private final Map<String, Registry> registries = new HashMap<>();
  private final Set<String> registryPaths = new HashSet<>();

  public Registry getRegistry(final String name) {
    return registries.get(name);
  }

  public Collection<Registry> getRegistries() {
    return registries.values();
  }

  public Registry createRegistry(final Registry registry) {
    registries.put(registry.getName(), registry);
    registryPaths.add(getRegistryPath(registry));
    return registry;
  }

  public Registry updateRegistry(final Registry oldRegistry, final Registry newRegistry) {
    // TODO: check no invalid operation is performed
    registries.remove(oldRegistry.getName());
    registryPaths.remove(getRegistryPath(oldRegistry));

    registries.put(newRegistry.getName(), newRegistry);
    registryPaths.add(getRegistryPath(newRegistry));
    return newRegistry;
  }

  public void deleteRegistry(final Registry registry) {
    registries.remove(registry.getName());
    registryPaths.remove(getRegistryPath(registry));
  }

  public boolean registryPathMatches(final String path) {
    for (final String registryPath : registryPaths) {
      if (path.startsWith(registryPath)) {
        return true;
      }
    }
    return false;
  }

  // TODO: put this method to the registry-api and make it publicly available
  private String getRegistryPath(final Registry registry) {
    return String.format("/registry/%s/", registry.getName());
  }
}
