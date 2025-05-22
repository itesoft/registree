package com.itesoft.registree.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.exception.NotFoundException;
import com.itesoft.registree.registry.api.listener.RegistryStoreListener;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistriesStore {
  @Lazy
  @Autowired(required = false)
  private List<RegistryStoreListener> listeners;

  private final Map<String, Registry> registries = new HashMap<>();

  public void store(final Registry registry) {
    registries.put(registry.getName(), registry);

    fireListeners(listener -> listener.registryStored(registry));
  }

  public void restore(final Registry registry) {
    registries.put(registry.getName(), registry);
  }

  public void unstore(final String name) {
    final Registry registry = registries.remove(name);

    fireListeners(listener -> listener.registryUnstored(registry));
  }

  public Collection<Registry> getRegistries() {
    return registries.values();
  }

  public Registry getRegistry(final String registryName) {
    final Registry registry = registries.get(registryName);
    if (registry == null) {
      throw new NotFoundException(String.format("Registry with name %s cannot be found", registryName));
    }
    return registry;
  }

  private void fireListeners(final Consumer<RegistryStoreListener> consumer) {
    if (listeners == null) {
      return;
    }
    for (final RegistryStoreListener listener : listeners) {
      consumer.accept(listener);
    }
  }
}
