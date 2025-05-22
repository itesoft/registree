package com.itesoft.registree.oci.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PreDestroy;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.dto.HostedRegistry;
import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.exception.UnprocessableException;
import com.itesoft.registree.oci.rest.OciRegistryRestControllerService;
import com.itesoft.registree.web.WebPathsByPortConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OciRegistries {
  private static final String PORT = "port";

  @Autowired
  private WebPathsByPortConfiguration webPathsByPortConfiguration;

  @Autowired
  private OciRegistryRestControllerService ociRegistryRestControllerService;

  @Autowired
  private ObjectMapper objectMapper;

  private final Map<Integer, Registry> registriesPerPort = new HashMap<>();

  @PreDestroy
  public void destroy() {
    for (final int port : registriesPerPort.keySet()) {
      ociRegistryRestControllerService.removeMapping(port);
    }
  }

  public Collection<Registry> getRegistries() {
    return registriesPerPort.values();
  }

  public Map<Integer, Registry> getRegistriesPerPort() {
    return registriesPerPort;
  }

  public Registry getRegistry(final String registryName) {
    return registriesPerPort.values()
      .stream()
      .filter(r -> r.getName().equals(registryName))
      .findFirst()
      .orElse(null);
  }

  public boolean isOciRegistry(final int port) {
    return registriesPerPort.containsKey(port);
  }

  public Registry createRegistry(final Registry registry) {
    final Registry ociRegistry = getOciRegistry(registry);
    final int port = ociRegistry.getProperty(PORT);
    registriesPerPort.put(port, ociRegistry);

    addWebMapping(port);

    return ociRegistry;
  }

  public Registry updateRegistry(final Registry oldRegistry, final Registry newRegistry) {
    // TODO: check no invalid operation is performed

    final Registry oldOciRegistry = toOciRegistry(oldRegistry);
    final int oldPort = oldOciRegistry.getProperty(PORT);
    registriesPerPort.remove(oldPort);

    final Registry newOciRegistry = getOciRegistry(newRegistry);
    final int newPort = newOciRegistry.getProperty(PORT);
    registriesPerPort.put(newPort, newOciRegistry);

    if (oldPort != newPort) {
      removeWebMapping(oldPort);
      addWebMapping(newPort);
    }

    return newOciRegistry;
  }

  public void deleteRegistry(final Registry registry) {
    final Registry cciRegistry = toOciRegistry(registry);
    final int port = cciRegistry.getProperty(PORT);

    removeWebMapping(port);
    registriesPerPort.remove(port);
  }

  private void addWebMapping(final int port) {
    ociRegistryRestControllerService.addMapping(port);
    webPathsByPortConfiguration.add(port, "/v2/");
  }

  private void removeWebMapping(final int port) {
    webPathsByPortConfiguration.remove(port, "/v2/");
    ociRegistryRestControllerService.removeMapping(port);
  }

  private Registry toOciRegistry(final Registry registry) {
    final Registry ociRegistry;
    try {
      if (registry instanceof HostedRegistry
        || registry instanceof GroupRegistry) {
        ociRegistry = registry;
      } else if (registry instanceof final ProxyRegistry proxyRegistry) {
        final OciProxyRegistry ociProxyRegistry =
          objectMapper.readValue(proxyRegistry.getConfiguration(),
                                 OciProxyRegistry.class);

        ociProxyRegistry.setName(proxyRegistry.getName());
        ociProxyRegistry.setFormat(proxyRegistry.getFormat());
        ociProxyRegistry.setType(proxyRegistry.getType());
        ociProxyRegistry.setConfiguration(proxyRegistry.getConfiguration());
        ociProxyRegistry.setDoStore(proxyRegistry.isDoStore());
        ociProxyRegistry.setStoragePath(proxyRegistry.getStoragePath());
        ociProxyRegistry.setProxyUrl(proxyRegistry.getProxyUrl());
        ociRegistry = ociProxyRegistry;
      } else {
        throw new UnprocessableException(String.format("Unexpected registry type %s", registry.getClass()));
      }

      final JsonNode jsonNode = objectMapper.readTree(registry.getConfiguration());
      final int port = jsonNode.get("port").asInt();
      ociRegistry.setProperty(PORT, port);

    } catch (final JsonProcessingException exception) {
      throw new UnprocessableException("Configuration is not valid", exception);
    }

    return ociRegistry;
  }

  private Registry getOciRegistry(final Registry registry) {
    final Registry ociRegistry = toOciRegistry(registry);
    validateRegistry(ociRegistry);
    return ociRegistry;
  }

  private void validateRegistry(final Registry registry) {
    final Integer port = registry.getProperty(PORT);
    if (registriesPerPort.containsKey(port)) {
      throw new UnprocessableException(String.format("The registry port %d is used for multiple registries, "
        + "configuration must specify a different port for each registry",
                                                     port));
    }
  }
}
