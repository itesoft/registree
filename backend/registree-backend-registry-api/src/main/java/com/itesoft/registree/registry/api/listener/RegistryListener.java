package com.itesoft.registree.registry.api.listener;

import com.itesoft.registree.dto.Registry;

public interface RegistryListener {
  String getFormat();

  Registry createRegistry(Registry registry);

  Registry updateRegistry(Registry oldRegistry, Registry newRegistry);

  void deleteRegistry(Registry registry);
}
