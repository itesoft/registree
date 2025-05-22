package com.itesoft.registree.maven.api;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.registry.api.RegistreeOperationPerformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MavenApiCallback {
  @Autowired
  private RegistreeOperationPerformer registreeOperationPerformer;

  public boolean componentExists(final Registry registry,
                                 final String groupId,
                                 final String artifactId,
                                 final String version) {
    return registreeOperationPerformer.componentExists(registry.getName(),
                                                       groupId,
                                                       artifactId,
                                                       version);
  }

  public String createComponent(final Registry registry,
                                final String groupId,
                                final String artifactId,
                                final String version) {
    return registreeOperationPerformer.createComponent(registry.getName(),
                                                       groupId,
                                                       artifactId,
                                                       version);
  }

  public String updateComponent(final Registry registry,
                                final String groupId,
                                final String artifactId,
                                final String version) {
    return registreeOperationPerformer.updateComponent(registry.getName(),
                                                       groupId,
                                                       artifactId,
                                                       version);
  }

  public boolean fileExists(final Registry registry,
                            final String path) {
    return registreeOperationPerformer.fileExists(registry.getName(),
                                                  path);
  }

  public void createFile(final Registry registry,
                         final String path,
                         final String contentType) {
    registreeOperationPerformer.createFile(registry.getName(),
                                           null,
                                           path,
                                           contentType);
  }

  public void updateFile(final Registry registry,
                         final String path,
                         final String contentType) {
    registreeOperationPerformer.updateFile(registry.getName(),
                                           null,
                                           path,
                                           contentType);
  }

  public void createFile(final Registry registry,
                         final String componentId,
                         final String path,
                         final String contentType) {
    registreeOperationPerformer.createFile(registry.getName(),
                                           componentId,
                                           path,
                                           contentType);
  }

  public void updateFile(final Registry registry,
                         final String componentId,
                         final String path,
                         final String contentType) {
    registreeOperationPerformer.updateFile(registry.getName(),
                                           componentId,
                                           path,
                                           contentType);
  }
}
