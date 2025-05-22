package com.itesoft.registree.registry.api.impl;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.registry.api.listener.RegistryStoreListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DispatcherRegistryListener implements RegistryStoreListener {
  @Autowired
  private DispatcherRegistries dispatcherRegistries;

  @Override
  public void registryStored(final Registry registry) {
    dispatcherRegistries.registryStored(registry);
  }

  @Override
  public void registryUnstored(final Registry registry) {
    dispatcherRegistries.registryUnstored(registry);
  }
}
