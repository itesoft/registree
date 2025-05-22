package com.itesoft.registree.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Consumer;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.CreateComponentArgs;
import com.itesoft.registree.dto.CreateFileArgs;
import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.File;
import com.itesoft.registree.dto.UpdateFileArgs;
import com.itesoft.registree.exception.ConflictException;
import com.itesoft.registree.exception.UnprocessableException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileControllerTest {
  private static final String REGISTRY_NAME = "reg_for_test";
  private static final String OTHER_REGISTRY_NAME = "other_reg_for_test";

  @Autowired
  private RegistryController registryController;

  @Autowired
  private ComponentController componentController;

  @Autowired
  private FileController fileController;

  private String componentId;
  private String otherComponentId;

  @BeforeAll
  public void setup() {
    CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .format("format")
      .name(REGISTRY_NAME)
      .type("type")
      .build();
    registryController.createRegistry(RequestContext.builder().build(),
                                      ResponseContext.builder().build(),
                                      createRegistryArgs);

    createRegistryArgs = CreateRegistryArgs.builder()
      .format("format")
      .name(OTHER_REGISTRY_NAME)
      .type("type")
      .build();
    registryController.createRegistry(RequestContext.builder().build(),
                                      ResponseContext.builder().build(),
                                      createRegistryArgs);

    final String group = "group";
    final String name = "createComponent";
    final String version = "1.2.3";

    CreateComponentArgs createComponentArgs = CreateComponentArgs.builder()
      .registryName(REGISTRY_NAME)
      .group(group)
      .name(name)
      .version(version)
      .build();
    Component component = componentController.createComponent(RequestContext.builder().build(),
                                                              ResponseContext.builder().build(),
                                                              createComponentArgs);
    componentId = component.getId();

    createComponentArgs = CreateComponentArgs.builder()
      .registryName(OTHER_REGISTRY_NAME)
      .group(group)
      .name(name)
      .version(version)
      .build();
    component = componentController.createComponent(RequestContext.builder().build(),
                                                    ResponseContext.builder().build(),
                                                    createComponentArgs);
    otherComponentId = component.getId();
  }

  @Test
  public void createFileWithComponent() throws Exception {
    final String path = "/createFileWithComponent";
    final String contentType = "plain/text";

    final Consumer<File> validator = new Consumer<>() {
      @Override
      public void accept(final File file) {
        assertNotNull(file.getId());
        assertEquals(REGISTRY_NAME, file.getRegistryName());
        assertEquals(componentId, file.getComponentId());
        assertEquals(path, file.getPath());
        assertEquals(contentType, file.getContentType());
        assertNotNull(file.getCreationDate());
        assertNotNull(file.getUpdateDate());
      }
    };

    final CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .componentId(componentId)
      .path(path)
      .contentType(contentType)
      .build();
    File file = fileController.createFile(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          createFileArgs);
    validator.accept(file);

    file = fileController.getFile(RequestContext.builder().build(),
                                  ResponseContext.builder().build(),
                                  file.getId());
    validator.accept(file);
  }

  @Test
  public void createFileWithRegistry() throws Exception {
    final String path = "/createFileWithRegistry";
    final String contentType = "plain/text";

    final Consumer<File> validator = new Consumer<>() {
      @Override
      public void accept(final File file) {
        assertNotNull(file.getId());
        assertEquals(REGISTRY_NAME, file.getRegistryName());
        assertNull(file.getComponentId());
        assertEquals(path, file.getPath());
        assertEquals(contentType, file.getContentType());
        assertNotNull(file.getCreationDate());
        assertNotNull(file.getUpdateDate());
      }
    };

    final CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .registryName(REGISTRY_NAME)
      .path(path)
      .contentType(contentType)
      .build();
    File file = fileController.createFile(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          createFileArgs);
    validator.accept(file);

    file = fileController.getFile(RequestContext.builder().build(),
                                  ResponseContext.builder().build(),
                                  file.getId());
    validator.accept(file);
  }

  @Test
  public void createFileWithRegistryAndComponent() throws Exception {
    final String path = "/createFileWithRegistryAndComponent";
    final String contentType = "plain/text";

    final Consumer<File> validator = new Consumer<>() {
      @Override
      public void accept(final File file) {
        assertNotNull(file.getId());
        assertEquals(REGISTRY_NAME, file.getRegistryName());
        assertEquals(componentId, file.getComponentId());
        assertEquals(path, file.getPath());
        assertEquals(contentType, file.getContentType());
        assertNotNull(file.getCreationDate());
        assertNotNull(file.getUpdateDate());
      }
    };

    final CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .registryName(REGISTRY_NAME)
      .componentId(componentId)
      .path(path)
      .contentType(contentType)
      .build();
    File file = fileController.createFile(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          createFileArgs);
    validator.accept(file);

    file = fileController.getFile(RequestContext.builder().build(),
                                  ResponseContext.builder().build(),
                                  file.getId());
    validator.accept(file);
  }

  @Test
  public void createFileWithRegistryAndComponentFromOtherRegistry() throws Exception {
    final String path = "/createFileWithRegistryAndComponentFromOtherRegistry";
    final String contentType = "plain/text";

    final CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .registryName(REGISTRY_NAME)
      .componentId(otherComponentId)
      .path(path)
      .contentType(contentType)
      .build();

    assertThrows(UnprocessableException.class,
                 () -> fileController.createFile(RequestContext.builder().build(),
                                                 ResponseContext.builder().build(),
                                                 createFileArgs));
  }

  @Test
  public void createSameFileTwice() throws Exception {
    final String path = "/createSameFileTwice";
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

    assertThrows(ConflictException.class,
                 () -> fileController.createFile(RequestContext.builder().build(),
                                                 ResponseContext.builder().build(),
                                                 createFileArgs));
  }

  @Test
  public void updateFile() throws Exception {
    final String path = "/updateFile";
    final String updatedPath = "/updateFile_Updated";
    final String contentType = "plain/text";

    final CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .componentId(componentId)
      .path(path)
      .contentType(contentType)
      .build();
    File file = fileController.createFile(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          createFileArgs);
    assertNotNull(file.getId());

    final UpdateFileArgs updateFileArgs = UpdateFileArgs.builder()
      .path(updatedPath)
      .contentType(contentType)
      .build();
    file = fileController.updateFile(RequestContext.builder().build(),
                                     ResponseContext.builder().build(),
                                     file.getId(),
                                     updateFileArgs);
    assertNotNull(file.getId());
    assertEquals(updatedPath, file.getPath());
    assertEquals(contentType, file.getContentType());
    assertNotNull(file.getCreationDate());
    assertNotNull(file.getUpdateDate());
    assertNotEquals(file.getCreationDate(), file.getUpdateDate());
  }

  @Test
  public void updateFileToExistingOne() throws Exception {
    final String firstPath = "/updateFileToExistingOne_first";
    final String secondPath = "/updateFileToExistingOne_second";
    final String contentType = "plain/text";

    CreateFileArgs createFileArgs = CreateFileArgs.builder()
      .componentId(componentId)
      .path(firstPath)
      .contentType(contentType)
      .build();
    File file = fileController.createFile(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          createFileArgs);
    assertNotNull(file.getId());

    createFileArgs = CreateFileArgs.builder()
      .componentId(componentId)
      .path(secondPath)
      .contentType(contentType)
      .build();
    file = fileController.createFile(RequestContext.builder().build(),
                                     ResponseContext.builder().build(),
                                     createFileArgs);
    assertNotNull(file.getId());
    final String secondFileId = file.getId();

    final UpdateFileArgs updateFileArgs = UpdateFileArgs.builder()
      .path(firstPath)
      .contentType(contentType)
      .build();

    assertThrows(ConflictException.class,
                 () -> fileController.updateFile(RequestContext.builder().build(),
                                                 ResponseContext.builder().build(),
                                                 secondFileId,
                                                 updateFileArgs));
  }
}
