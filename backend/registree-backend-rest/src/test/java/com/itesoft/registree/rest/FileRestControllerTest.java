package com.itesoft.registree.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.ws.rs.WebApplicationException;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.ComponentController;
import com.itesoft.registree.controller.FileController;
import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.CreateComponentArgs;
import com.itesoft.registree.dto.CreateFileArgs;
import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class FileRestControllerTest extends RestControllerTest {
  private static final String REGISTRY_NAME = "reg_for_test";

  @Autowired
  private ComponentController componentController;

  @Autowired
  private FileController fileController;

  private String componentId;

  @BeforeAll
  public void setup() {
    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .format("format")
      .name(REGISTRY_NAME)
      .type("type")
      .build();
    registryClient.createRegistry(createRegistryArgs);

    final String group = "group";
    final String name = "createComponent";
    final String version = "1.2.3";

    final CreateComponentArgs createComponentArgs = CreateComponentArgs.builder()
      .registryName(REGISTRY_NAME)
      .group(group)
      .name(name)
      .version(version)
      .build();
    final Component component = componentController.createComponent(RequestContext.builder().build(),
                                                                    ResponseContext.builder().build(),
                                                                    createComponentArgs);
    componentId = component.getId();
  }

  @Test
  public void deleteFile() throws Exception {
    final String path = "/deleteFile";
    final String contentType = "plain/text";

    final CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .componentId(componentId)
      .path(path)
      .contentType(contentType)
      .build();
    final File file = fileController.createFile(RequestContext.builder().build(),
                                                ResponseContext.builder().build(),
                                                createFileArgs);
    assertNotNull(file.getId());

    fileClient.getFile(file.getId());

    fileClient.deleteFile(file.getId(), null);

    final WebApplicationException exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> fileClient.getFile(file.getId()));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());

  }
}
