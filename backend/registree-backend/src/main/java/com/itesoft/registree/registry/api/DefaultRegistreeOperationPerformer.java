package com.itesoft.registree.registry.api;

import jakarta.transaction.Transactional;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.ComponentController;
import com.itesoft.registree.controller.FileController;
import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.CreateComponentArgs;
import com.itesoft.registree.dto.CreateFileArgs;
import com.itesoft.registree.dto.File;
import com.itesoft.registree.dto.Gav;
import com.itesoft.registree.dto.UpdateComponentArgs;
import com.itesoft.registree.dto.UpdateFileArgs;

import org.springframework.beans.factory.annotation.Autowired;

@Transactional(rollbackOn = Throwable.class)
@org.springframework.stereotype.Component
public class DefaultRegistreeOperationPerformer implements RegistreeOperationPerformer {
  @Autowired
  private ComponentController componentController;

  @Autowired
  private FileController fileController;

  @Override
  public boolean componentExists(final String registryName,
                                 final String group,
                                 final String name,
                                 final String version) {
    final Gav gav = Gav.builder().group(group).name(name).version(version).build();
    return componentController.componentExists(RequestContext.builder().build(),
                                               ResponseContext.builder().build(),
                                               registryName,
                                               gav.toString());
  }

  @Override
  public String createComponent(final String registryName,
                                final String group,
                                final String name,
                                final String version) {
    final CreateComponentArgs createComponentArgs = CreateComponentArgs.builder()
      .registryName(registryName)
      .group(group)
      .name(name)
      .version(version)
      .build();
    final Component component =
      componentController.createComponent(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          createComponentArgs);
    return component.getId();
  }

  @Override
  public String updateComponent(final String registryName,
                                final String group,
                                final String name,
                                final String version) {
    final Gav gav = Gav.builder()
      .group(group)
      .name(name)
      .version(version)
      .build();
    final UpdateComponentArgs updateComponentArgs = UpdateComponentArgs.builder()
      .group(group)
      .name(name)
      .version(version)
      .build();
    final Component component =
      componentController.updateComponent(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          registryName,
                                          gav.toString(),
                                          updateComponentArgs);
    return component.getId();
  }

  @Override
  public boolean fileExists(final String registryName, final String path) {
    return fileController.fileExists(RequestContext.builder().build(),
                                     ResponseContext.builder().build(),
                                     registryName,
                                     path);
  }

  @Override
  public String createFile(final String registryName,
                           final String componentId,
                           final String path,
                           final String contentType) {
    final CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .registryName(registryName)
      .componentId(componentId)
      .path(path)
      .contentType(contentType)
      .build();
    final File file =
      fileController.createFile(RequestContext.builder().build(),
                                ResponseContext.builder().build(),
                                createFileArgs);
    return file.getId();
  }

  @Override
  public String updateFile(final String registryName,
                           final String componentId,
                           final String path,
                           final String contentType) {
    final UpdateFileArgs updateFileArgs = UpdateFileArgs.builder()
      .path(path)
      .contentType(contentType)
      .build();
    final File file =
      fileController.updateFile(RequestContext.builder().build(),
                                ResponseContext.builder().build(),
                                registryName,
                                path,
                                updateFileArgs);
    return file.getId();
  }
}
