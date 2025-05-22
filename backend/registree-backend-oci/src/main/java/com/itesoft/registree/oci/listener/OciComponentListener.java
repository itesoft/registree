package com.itesoft.registree.oci.listener;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.oci.config.OciRegistries;
import com.itesoft.registree.oci.storage.RepositoryStorage;
import com.itesoft.registree.registry.api.listener.ComponentOperationListener;

import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class OciComponentListener implements ComponentOperationListener {
  @Autowired
  private OciRegistries ociRegistries;

  @Autowired
  private RepositoryStorage repositoryStorage;

  @Override
  public void componentCreated(final Component component) {
  }

  @Override
  public void componentUpdated(final Component oldComponent, final Component newComponent) {
  }

  @Override
  public void componentDeleting(final Component component) {
    final Registry registry = ociRegistries.getRegistry(component.getRegistryName());
    if (registry == null) {
      return;
    }
    try {
      repositoryStorage.deleteManifest(registry,
                                       component.getName(),
                                       component.getVersion());
    } catch (final Exception exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }
}
