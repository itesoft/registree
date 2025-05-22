package com.itesoft.registree.registry.api.listener;

import com.itesoft.registree.dto.Component;

public interface ComponentOperationListener {
  void componentCreated(Component component);

  void componentUpdated(Component oldComponent, Component newComponent);

  void componentDeleting(Component component);
}
