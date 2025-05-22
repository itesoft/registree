package com.itesoft.registree.maven.config;

import static com.itesoft.registree.maven.config.MavenConstants.FORMAT;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.registry.api.listener.RegistryListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MavenRegistryListener implements RegistryListener {
  @Autowired
  private MavenRegistries mavenRegistries;

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  public Registry createRegistry(final Registry registry) {
    return mavenRegistries.createRegistry(registry);
  }

  @Override
  public Registry updateRegistry(final Registry oldRegistry, final Registry newRegistry) {
    return mavenRegistries.updateRegistry(oldRegistry, newRegistry);
  }

  @Override
  public void deleteRegistry(final Registry registry) {
    mavenRegistries.deleteRegistry(registry);
  }
}
