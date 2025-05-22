package com.itesoft.registree.maven.listener;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.maven.config.MavenRegistries;
import com.itesoft.registree.maven.storage.ArtifactStorage;
import com.itesoft.registree.registry.api.listener.ComponentOperationListener;

import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class MavenComponentListener implements ComponentOperationListener {
  @Autowired
  private MavenRegistries mavenRegistries;

  @Autowired
  private ArtifactStorage artifactStorage;

  @Override
  public void componentCreated(final Component component) {
  }

  @Override
  public void componentUpdated(final Component oldComponent, final Component newComponent) {
  }

  @Override
  public void componentDeleting(final Component component) {
    final Registry registry = mavenRegistries.getRegistry(component.getRegistryName());
    if (registry == null) {
      return;
    }
    try {
      artifactStorage.deleteArtifact(registry,
                                     component.getGroup(),
                                     component.getName(),
                                     component.getVersion());
    } catch (final Exception exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }
}
