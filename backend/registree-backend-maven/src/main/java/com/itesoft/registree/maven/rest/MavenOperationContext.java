package com.itesoft.registree.maven.rest;

import java.util.Map;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.maven.config.MavenRegistries;

public class MavenOperationContext {
  private final Map<String, MavenMetadataManager> metadataManagers;
  private final Map<String, MavenArtifactManager> artifactManagers;
  private final Map<String, MavenChecksumManager> checksumManagers;
  private final MavenRegistries mavenRegistries;
  private final Registry registry;

  public MavenOperationContext(final Map<String, MavenMetadataManager> metadataManagers,
                               final Map<String, MavenArtifactManager> artifactManagers,
                               final Map<String, MavenChecksumManager> checksumManagers,
                               final MavenRegistries mavenRegistries,
                               final Registry registry) {
    this.metadataManagers = metadataManagers;
    this.artifactManagers = artifactManagers;
    this.checksumManagers = checksumManagers;
    this.mavenRegistries = mavenRegistries;
    this.registry = registry;
  }

  public Registry getRegistry() {
    return registry;
  }

  public MavenOperationContext createSubContext(final String registryName) {
    for (final Registry registry : mavenRegistries.getRegistries()) {
      if (registryName.equals(registry.getName())) {
        return new MavenOperationContext(metadataManagers,
                                         artifactManagers,
                                         checksumManagers,
                                         mavenRegistries,
                                         registry);
      }
    }
    throw new IllegalArgumentException(String.format("Cannot find registry with name %s", registryName));
  }

  public MavenMetadataManager getMetadataManager() {
    return metadataManagers.get(registry.getType());
  }

  public MavenArtifactManager getArtifactManager() {
    return artifactManagers.get(registry.getType());
  }

  public MavenChecksumManager getChecksumManager() {
    return checksumManagers.get(registry.getType());
  }

  @SuppressWarnings("unchecked")
  public <T> T getMavenManager(final Class<T> managerType) {
    if (MavenMetadataManager.class.equals(managerType)) {
      return (T) getMetadataManager();
    } else if (MavenArtifactManager.class.equals(managerType)) {
      return (T) getArtifactManager();
    } else {
      return (T) getChecksumManager();
    }
  }
}
