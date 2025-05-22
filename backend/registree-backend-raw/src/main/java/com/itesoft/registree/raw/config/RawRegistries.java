package com.itesoft.registree.raw.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dto.Registry;

import org.springframework.stereotype.Component;

@Component
public class RawRegistries {
  private final Map<String, Registry> registries = new HashMap<>();

  public Registry getRegistry(final String name) {
    return registries.get(name);
  }

  public Collection<Registry> getRegistries() {
    return registries.values();
  }

  public Registry createRegistry(final Registry registry) {
    registries.put(registry.getName(), registry);
    return registry;
  }

  public Registry updateRegistry(final Registry oldRegistry, final Registry newRegistry) {
    // TODO: check no invalid operation is performed
    registries.remove(oldRegistry.getName());

    registries.put(newRegistry.getName(), newRegistry);
    return newRegistry;
  }

  public void deleteRegistry(final Registry registry) {
    registries.remove(registry.getName());
  }
}
