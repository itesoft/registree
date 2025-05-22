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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

public class ComponentRestControllerTest extends RestControllerTest {
  private static final String REGISTRY_NAME = "reg_for_test";

  @LocalServerPort
  private int port;

  @Autowired
  private ComponentController componentController;

  @Autowired
  private FileController fileController;

  @BeforeAll
  public void setup() {
    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .format("format")
      .name(REGISTRY_NAME)
      .type("type")
      .build();
    registryClient.createRegistry(createRegistryArgs);
  }

  @Test
  public void deleteComponent() throws Exception {
    final String group = "group";
    final String name = "deleteComponent";
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
    assertNotNull(component.getId());

    componentClient.getComponent(component.getId());

    componentClient.deleteComponent(component.getId(), null);

    final WebApplicationException exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> componentClient.getComponent(component.getId()));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());
  }

  @Test
  public void deleteComponentContainingFiles() throws Exception {
    final String group = "group";
    final String name = "deleteComponentContainingFiles";
    final String version = "1.2.3";
    final String contentType = "text/plain";

    final CreateComponentArgs createComponentArgs = CreateComponentArgs.builder()
      .registryName(REGISTRY_NAME)
      .group(group)
      .name(name)
      .version(version)
      .build();
    final Component component = componentController.createComponent(RequestContext.builder().build(),
                                                                    ResponseContext.builder().build(),
                                                                    createComponentArgs);
    assertNotNull(component.getId());

    CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .componentId(component.getId())
      .contentType(contentType)
      .path("/first")
      .build();
    fileController.createFile(RequestContext.builder().build(),
                              ResponseContext.builder().build(),
                              createFileArgs);

    createFileArgs = CreateFileArgs.builder()
      .componentId(component.getId())
      .contentType(contentType)
      .path("/second")
      .build();
    fileController.createFile(RequestContext.builder().build(),
                              ResponseContext.builder().build(),
                              createFileArgs);

    fileClient.getFile(REGISTRY_NAME,
                       "/first");
    fileClient.getFile(REGISTRY_NAME,
                       "/second");
    componentClient.getComponent(component.getId());

    componentClient.deleteComponent(component.getId(), null);

    WebApplicationException exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> componentClient.getComponent(component.getId()));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());

    exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> fileClient.getFile(REGISTRY_NAME,
                                            "/first"));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());

    exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> fileClient.getFile(REGISTRY_NAME,
                                            "/second"));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());
  }
}
