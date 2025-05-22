package com.itesoft.registree.npm.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.dto.HostedRegistry;
import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.exception.UnprocessableException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class NpmRegistries {
  @Autowired
  private ObjectMapper objectMapper;

  private final Map<String, Registry> registries = new HashMap<>();

  public Registry getRegistry(final String name) {
    return registries.get(name);
  }

  public Collection<Registry> getRegistries() {
    return registries.values();
  }

  public Registry createRegistry(final Registry registry) {
    final Registry npmRegistry = toNpmRegistry(registry);
    registries.put(npmRegistry.getName(), npmRegistry);
    return npmRegistry;
  }

  public Registry updateRegistry(final Registry oldRegistry, final Registry newRegistry) {
    // TODO: check no invalid operation is performed
    registries.remove(oldRegistry.getName());

    final Registry npmRegistry = toNpmRegistry(newRegistry);
    registries.put(npmRegistry.getName(), npmRegistry);
    return npmRegistry;
  }

  public void deleteRegistry(final Registry registry) {
    registries.remove(registry.getName());
  }

  public void validateRegistries() {
    validateGlobalConfiguration();
  }

  private Registry toNpmRegistry(final Registry registry) {
    final Registry npmRegistry;
    try {
      if (registry instanceof final HostedRegistry hostedRegistry) {
        npmRegistry = registry;
        validateHostedRegistry(hostedRegistry);
      } else if (registry instanceof final GroupRegistry groupRegistry) {
        npmRegistry = registry;
        validateGroupRegistry(groupRegistry);
      } else if (registry instanceof final ProxyRegistry proxyRegistry) {
        // TODO: refactore this
        final NpmProxyRegistry npmProxyRegistry =
          objectMapper.readValue(registry.getConfiguration(),
                                 NpmProxyRegistry.class);

        npmProxyRegistry.setName(proxyRegistry.getName());
        npmProxyRegistry.setFormat(proxyRegistry.getFormat());
        npmProxyRegistry.setType(proxyRegistry.getType());
        npmProxyRegistry.setDoStore(proxyRegistry.isDoStore());
        npmProxyRegistry.setStoragePath(proxyRegistry.getStoragePath());
        npmProxyRegistry.setProxyUrl(proxyRegistry.getProxyUrl());
        npmProxyRegistry.setConfiguration(registry.getConfiguration());

        validateProxyRegistry(npmProxyRegistry);

        npmRegistry = npmProxyRegistry;
      } else {
        throw new UnprocessableException(String.format("Unexpected registry type %s", registry.getClass()));
      }
    } catch (final JsonProcessingException exception) {
      throw new UnprocessableException("Configuration is not valid", exception);
    }

    return npmRegistry;
  }

  private void validateGlobalConfiguration() {
    for (final Registry registry : registries.values()) {
      if (registry instanceof final GroupRegistry groupRegistry) {
        final List<String> memberNames = groupRegistry.getMemberNames();
        if (memberNames != null) {
          for (final String memberName : memberNames) {
            if (groupRegistry.getName().equals(memberName)) {
              throw new UnprocessableException("The group registry self-references himself");
            }

            if (!registries.values().stream().map(e -> e.getName()).collect(Collectors.toList()).contains(memberName)) {
              throw new UnprocessableException(String.format("The group registry member named %s cannot be found", memberName));
            }
          }
        }
      }
    }
  }

  private void validateGroupRegistry(final GroupRegistry groupRegistry) {
    validateRegistry(groupRegistry);
  }

  private void validateProxyRegistry(final NpmProxyRegistry npmProxyRegistry) {
    validateRegistry(npmProxyRegistry);
    if (npmProxyRegistry.getProxyUrl().endsWith("/")) {
      throw new UnprocessableException("The proxy URL of a proxy registry must not end with /");
    }

    if (npmProxyRegistry.isDoStore() && ObjectUtils.isEmpty(npmProxyRegistry.getStoragePath())) {
      throw new UnprocessableException("The proxy registry must define a storage path when doStore is true");
    }
  }

  private void validateHostedRegistry(final HostedRegistry hostedRegistry) {
    validateRegistry(hostedRegistry);
  }

  private void validateRegistry(final Registry registry) {
  }
}
