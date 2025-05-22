package com.itesoft.registree.npm.listener;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.npm.config.NpmRegistries;
import com.itesoft.registree.npm.storage.PackageStorage;
import com.itesoft.registree.registry.api.listener.ComponentOperationListener;

import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class NpmComponentListener implements ComponentOperationListener {
  @Autowired
  private NpmRegistries npmRegistries;

  @Autowired
  private PackageStorage packageStorage;

  @Override
  public void componentCreated(final Component component) {
  }

  @Override
  public void componentUpdated(final Component oldComponent, final Component newComponent) {
  }

  @Override
  public void componentDeleting(final Component component) {
    final Registry registry = npmRegistries.getRegistry(component.getRegistryName());
    if (registry == null) {
      return;
    }
    try {
      packageStorage.deleteTarball(registry,
                                   component.getGroup(),
                                   component.getName(),
                                   component.getVersion());
    } catch (final Exception exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }
}
