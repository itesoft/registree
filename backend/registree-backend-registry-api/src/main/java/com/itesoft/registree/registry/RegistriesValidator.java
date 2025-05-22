package com.itesoft.registree.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.exception.UnprocessableException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class RegistriesValidator {
  @Autowired
  private RegistriesStore registriesStore;

  @Autowired
  private Validator validator;

  public void validateCreation(final Registry registry) {
    validateRegistry(registry);

    if (registry instanceof final GroupRegistry groupRegistry) {
      final Collection<Registry> perFormatRegistries = getPerFormatRegistries(groupRegistry.getFormat());
      validateGroupRegistry(perFormatRegistries, groupRegistry);
    }
  }

  public void validateDeletion(final Registry registry) {
    final Collection<Registry> perFormatRegistries = getPerFormatRegistries(registry.getFormat());
    for (final Registry r : perFormatRegistries) {
      if (r instanceof final GroupRegistry groupRegistry) {
        for (final String memberName : groupRegistry.getMemberNames()) {
          if (memberName.equals(registry.getName())) {
            throw new UnprocessableException(String.format("The registry %s is referenced by a group, deletion failed",
                                                           registry.getName()));
          }
        }
      }
    }
  }

  public void validate(final Collection<Registry> registries) {
    final Map<String, Collection<Registry>> registriesPerFormat = new HashMap<>();
    for (final Registry registry : registries) {
      validateRegistry(registry);

      final String format = registry.getFormat();
      Collection<Registry> formatRegistries = registriesPerFormat.get(format);
      if (formatRegistries == null) {
        formatRegistries = new ArrayList<>();
        registriesPerFormat.put(format, formatRegistries);
      }
      formatRegistries.add(registry);
    }

    for (final Collection<Registry> perFormatRegistries : registriesPerFormat.values()) {
      validatePerFormatConfiguration(perFormatRegistries);
    }
  }

  private void validateRegistry(final Registry registry) {
    final Errors errors = validator.validateObject(registry);
    errors.failOnError(UnprocessableException::new);

    if (registry instanceof final ProxyRegistry proxyRegistry) {
      validateProxyRegistry(proxyRegistry);
    }
  }

  private void validateProxyRegistry(final ProxyRegistry proxyRegistry) {
    if (proxyRegistry.getProxyUrl().endsWith("/")) {
      throw new UnprocessableException("The proxy URL of a proxy registry must not end with /");
    }

    if (proxyRegistry.isDoStore() && ObjectUtils.isEmpty(proxyRegistry.getStoragePath())) {
      throw new UnprocessableException("The proxy registry must define a storage path when doStore is true");
    }
  }

  private void validatePerFormatConfiguration(final Collection<Registry> perFormatRegistries) {
    for (final Registry registry : perFormatRegistries) {
      if (registry instanceof final GroupRegistry groupRegistry) {
        validateGroupRegistry(perFormatRegistries, groupRegistry);
      }
    }
  }

  private void validateGroupRegistry(final Collection<Registry> perFormatRegistries,
                                     final GroupRegistry groupRegistry) {
    final List<String> memberNames = groupRegistry.getMemberNames();
    if (memberNames != null) {
      for (final String memberName : memberNames) {
        if (groupRegistry.getName().equals(memberName)) {
          throw new UnprocessableException("The group registry self-references himself");
        }

        if (!perFormatRegistries.stream().map(e -> e.getName()).collect(Collectors.toList()).contains(memberName)) {
          throw new UnprocessableException(String.format("The group registry member named %s cannot be found", memberName));
        }
      }
    }
  }

  private Collection<Registry> getPerFormatRegistries(final String format) {
    final Collection<Registry> perFormatRegistries = new ArrayList<>();
    for (final Registry registry : registriesStore.getRegistries()) {
      if (format.equals(registry.getFormat())) {
        perFormatRegistries.add(registry);
      }
    }
    return perFormatRegistries;
  }
}
