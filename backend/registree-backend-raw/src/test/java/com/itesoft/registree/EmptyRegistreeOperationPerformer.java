package com.itesoft.registree;

import com.itesoft.registree.registry.api.RegistreeOperationPerformer;

import org.springframework.stereotype.Component;

@Component
public class EmptyRegistreeOperationPerformer implements RegistreeOperationPerformer {
  @Override
  public boolean componentExists(final String registryName,
                                 final String group,
                                 final String name,
                                 final String version) {
    return false;
  }

  @Override
  public String createComponent(final String registryName,
                                final String group,
                                final String name,
                                final String version) {
    return null;
  }

  @Override
  public String updateComponent(final String registryName,
                                final String group,
                                final String name,
                                final String version) {
    return null;
  }

  @Override
  public boolean fileExists(final String registryName,
                            final String path) {
    return false;
  }

  @Override
  public String createFile(final String registryName,
                           final String componentId,
                           final String path,
                           final String type) {
    return null;
  }

  @Override
  public String updateFile(final String registryName,
                           final String componentId,
                           final String path,
                           final String contentType) {
    return null;
  }
}
