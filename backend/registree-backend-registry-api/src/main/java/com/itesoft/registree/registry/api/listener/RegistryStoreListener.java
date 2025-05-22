package com.itesoft.registree.registry.api.listener;

import com.itesoft.registree.dto.Registry;

public interface RegistryStoreListener {
  void registryStored(Registry registry);

  void registryUnstored(Registry registry);
}
