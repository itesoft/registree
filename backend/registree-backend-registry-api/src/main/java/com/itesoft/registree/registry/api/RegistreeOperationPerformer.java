package com.itesoft.registree.registry.api;

public interface RegistreeOperationPerformer {
  boolean componentExists(String registryName,
                          String group,
                          String name,
                          String version);

  String createComponent(String registryName,
                         String group,
                         String name,
                         String version);

  String updateComponent(String registryName,
                         String group,
                         String name,
                         String version);

  boolean fileExists(String registryName,
                     String path);

  String createFile(String registryName,
                    String componentId,
                    String path,
                    String contentType);

  String updateFile(String registryName,
                    String componentId,
                    String path,
                    String contentType);
}
