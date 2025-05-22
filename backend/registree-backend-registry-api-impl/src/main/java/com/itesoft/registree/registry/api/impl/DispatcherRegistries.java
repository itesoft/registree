package com.itesoft.registree.registry.api.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.web.WebPathsByPortConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DispatcherRegistries {
  @Autowired
  private WebPathsByPortConfiguration webPathsByPortConfiguration;

  private final Map<String, Registry> registriesPerName = new HashMap<>();

  public Registry getRegistry(final String registryName) {
    return registriesPerName.get(registryName);
  }

  public Collection<Registry> getRegistries() {
    return registriesPerName.values();
  }

  public void registryStored(final Registry registry) {
    webPathsByPortConfiguration.add(getRegistryPath(registry));
    registriesPerName.put(registry.getName(), registry);
  }

  public void registryUnstored(final Registry registry) {
    webPathsByPortConfiguration.remove(getRegistryPath(registry));
    registriesPerName.remove(registry.getName());
  }

  private String getRegistryPath(final Registry registry) {
    return String.format("/registry/%s/", registry.getName());
  }
}
