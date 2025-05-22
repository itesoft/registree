package com.itesoft.registree.oci.config;

import static com.itesoft.registree.oci.config.OciConstants.FORMAT;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.registry.api.listener.RegistryListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OciRegistryListener implements RegistryListener {
  @Autowired
  private OciRegistries ociRegistries;

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  public Registry createRegistry(final Registry registry) {
    return ociRegistries.createRegistry(registry);
  }

  @Override
  public Registry updateRegistry(final Registry oldRegistry, final Registry newRegistry) {
    return ociRegistries.updateRegistry(oldRegistry, newRegistry);
  }

  @Override
  public void deleteRegistry(final Registry registry) {
    ociRegistries.deleteRegistry(registry);
  }
}
