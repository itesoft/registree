package com.itesoft.registree;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.registry.api.listener.RegistryListener;

import org.springframework.stereotype.Component;

@Component
public class TestCustomRegistryListener implements RegistryListener {
  @Override
  public String getFormat() {
    return "custom";
  }

  @Override
  public Registry createRegistry(final Registry registry) {
    return registry;
  }

  @Override
  public Registry updateRegistry(final Registry oldRegistry, final Registry newRegistry) {
    return newRegistry;
  }

  @Override
  public void deleteRegistry(final Registry registry) {
  }
}
