package com.itesoft.registree.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.RegistryController;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.exception.UnprocessableException;
import com.itesoft.registree.registry.RegistriesStore;
import com.itesoft.registree.registry.api.listener.RegistryListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@DependsOn("dataSourceScriptDatabaseInitializer")
public class RegistreeConfigurationLoader {
  private final Map<String, RegistryListener> listeners = new HashMap<>();

  @Autowired
  private RegistriesStore registriesStore;

  @Lazy
  @Autowired
  private void setListeners(final Collection<RegistryListener> listeners) {
    for (final RegistryListener listener : listeners) {
      this.listeners.put(listener.getFormat(), listener);
    }
  }

  @Autowired
  private RegistryController registryController;

  @PostConstruct
  public void loadRegistries() {
    final List<Registry> registries =
      registryController.searchRegistries(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          null,
                                          null,
                                          null,
                                          1000); // TODO: use paginated search

    for (final Registry registry : registries) {
      final RegistryListener listener = listeners.get(registry.getFormat());
      if (listener == null) {
        throw new UnprocessableException("Startup failed, listeners missing");
      }
      final Registry storedRegistry = listener.createRegistry(registry);
      registriesStore.store(storedRegistry);
    }
  }
}
