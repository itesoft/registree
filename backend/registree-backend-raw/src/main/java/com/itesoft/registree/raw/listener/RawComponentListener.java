package com.itesoft.registree.raw.listener;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.raw.config.RawRegistries;
import com.itesoft.registree.raw.storage.FileStorage;
import com.itesoft.registree.registry.api.listener.ComponentOperationListener;

import org.springframework.beans.factory.annotation.Autowired;

@org.springframework.stereotype.Component
public class RawComponentListener implements ComponentOperationListener {
  @Autowired
  private RawRegistries rawRegistries;

  @Autowired
  private FileStorage fileStorage;

  @Override
  public void componentCreated(final Component component) {
  }

  @Override
  public void componentUpdated(final Component oldComponent, final Component newComponent) {
  }

  @Override
  public void componentDeleting(final Component component) {
    final Registry registry = rawRegistries.getRegistry(component.getRegistryName());
    if (registry == null) {
      return;
    }
    try {
      fileStorage.deleteFile(registry,
                             component.getName());
    } catch (final Exception exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }
}
