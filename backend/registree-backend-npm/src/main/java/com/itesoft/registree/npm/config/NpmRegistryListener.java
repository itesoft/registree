package com.itesoft.registree.npm.config;

import static com.itesoft.registree.npm.config.NpmConstants.FORMAT;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.registry.api.listener.RegistryListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NpmRegistryListener implements RegistryListener {
  @Autowired
  private NpmRegistries npmRegistries;

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  public Registry createRegistry(final Registry registry) {
    return npmRegistries.createRegistry(registry);
  }

  @Override
  public Registry updateRegistry(final Registry oldRegistry, final Registry newRegistry) {
    return npmRegistries.updateRegistry(oldRegistry, newRegistry);
  }

  @Override
  public void deleteRegistry(final Registry registry) {
    npmRegistries.deleteRegistry(registry);
  }
}
