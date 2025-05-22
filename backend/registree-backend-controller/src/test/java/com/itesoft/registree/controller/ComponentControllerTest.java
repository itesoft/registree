package com.itesoft.registree.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.CreateComponentArgs;
import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.UpdateComponentArgs;
import com.itesoft.registree.exception.ConflictException;

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
public class ComponentControllerTest {
  private static final String REGISTRY_NAME = "reg_for_test";

  @Autowired
  private RegistryController registryController;

  @Autowired
  private ComponentController componentController;

  @BeforeAll
  public void setup() {
    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .format("format")
      .name(REGISTRY_NAME)
      .type("type")
      .build();
    registryController.createRegistry(RequestContext.builder().build(),
                                      ResponseContext.builder().build(),
                                      createRegistryArgs);
  }

  @Test
  public void createComponent() throws Exception {
    final String group = "group";
    final String name = "createComponent";
    final String version = "1.2.3";

    final CreateComponentArgs createComponentArgs = CreateComponentArgs.builder()
      .registryName(REGISTRY_NAME)
      .group(group)
      .name(name)
      .version(version)
      .build();
    Component component = componentController.createComponent(RequestContext.builder().build(),
                                                              ResponseContext.builder().build(),
                                                              createComponentArgs);
    assertNotNull(component.getId());
    assertEquals(REGISTRY_NAME, component.getRegistryName());
    assertEquals(group, component.getGroup());
    assertEquals(name, component.getName());
    assertEquals(version, component.getVersion());
    assertNotNull(component.getCreationDate());
    assertNotNull(component.getUpdateDate());

    component = componentController.getComponent(RequestContext.builder().build(),
                                                 ResponseContext.builder().build(),
                                                 component.getId());
    assertEquals(REGISTRY_NAME, component.getRegistryName());
    assertEquals(group, component.getGroup());
    assertEquals(name, component.getName());
    assertEquals(version, component.getVersion());
    assertNotNull(component.getCreationDate());
    assertNotNull(component.getUpdateDate());

    final String gav = String.format("%s:%s:%s", group, name, version);
    component = componentController.getComponent(RequestContext.builder().build(),
                                                 ResponseContext.builder().build(),
                                                 REGISTRY_NAME,
                                                 gav);
    assertEquals(REGISTRY_NAME, component.getRegistryName());
    assertEquals(group, component.getGroup());
    assertEquals(name, component.getName());
    assertEquals(version, component.getVersion());
    assertNotNull(component.getCreationDate());
    assertNotNull(component.getUpdateDate());
  }

  @Test
  public void createSameComponentTwice() throws Exception {
    final String group = "group";
    final String name = "createSameComponentTwice";
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

    assertThrows(ConflictException.class,
                 () -> componentController.createComponent(RequestContext.builder().build(),
                                                           ResponseContext.builder().build(),
                                                           createComponentArgs));
  }

  @Test
  public void createComponentWithNoGroup() throws Exception {
    final String name = "createComponentWithNoGroup";
    final String version = "1.2.3";

    final CreateComponentArgs createComponentArgs = CreateComponentArgs.builder()
      .registryName(REGISTRY_NAME)
      .name(name)
      .version(version)
      .build();
    Component component = componentController.createComponent(RequestContext.builder().build(),
                                                              ResponseContext.builder().build(),
                                                              createComponentArgs);
    assertNotNull(component.getId());
    assertEquals(REGISTRY_NAME, component.getRegistryName());
    assertNull(component.getGroup());
    assertEquals(name, component.getName());
    assertEquals(version, component.getVersion());
    assertNotNull(component.getCreationDate());
    assertNotNull(component.getUpdateDate());

    component = componentController.getComponent(RequestContext.builder().build(),
                                                 ResponseContext.builder().build(),
                                                 component.getId());
    assertEquals(REGISTRY_NAME, component.getRegistryName());
    assertNull(component.getGroup());
    assertEquals(name, component.getName());
    assertEquals(version, component.getVersion());
    assertNotNull(component.getCreationDate());
    assertNotNull(component.getUpdateDate());

    final String gav = String.format("%s:%s", name, version);
    component = componentController.getComponent(RequestContext.builder().build(),
                                                 ResponseContext.builder().build(),
                                                 REGISTRY_NAME,
                                                 gav);
    assertEquals(REGISTRY_NAME, component.getRegistryName());
    assertNull(component.getGroup());
    assertEquals(name, component.getName());
    assertEquals(version, component.getVersion());
    assertNotNull(component.getCreationDate());
    assertNotNull(component.getUpdateDate());
  }

  @Test
  public void updateComponent() throws Exception {
    final String group = "group";
    final String name = "updateComponent";
    final String version = "1.2.3";
    final String updatedGroup = "group_up";
    final String updatedName = "updateComponent_up";
    final String updatedVersion = "6.5.4";

    final CreateComponentArgs createComponentArgs = CreateComponentArgs.builder()
      .registryName(REGISTRY_NAME)
      .group(group)
      .name(name)
      .version(version)
      .build();
    Component component = componentController.createComponent(RequestContext.builder().build(),
                                                              ResponseContext.builder().build(),
                                                              createComponentArgs);
    assertNotNull(component.getId());

    final UpdateComponentArgs updateComponentArgs = UpdateComponentArgs.builder()
      .group(updatedGroup)
      .name(updatedName)
      .version(updatedVersion)
      .build();

    component = componentController.updateComponent(RequestContext.builder().build(),
                                                    ResponseContext.builder().build(),
                                                    component.getId(),
                                                    updateComponentArgs);
    assertEquals(REGISTRY_NAME, component.getRegistryName());
    assertEquals(updatedGroup, component.getGroup());
    assertEquals(updatedName, component.getName());
    assertEquals(updatedVersion, component.getVersion());
    assertNotNull(component.getCreationDate());
    assertNotNull(component.getUpdateDate());
    assertNotEquals(component.getCreationDate(), component.getUpdateDate());

    component = componentController.updateComponent(RequestContext.builder().build(),
                                                    ResponseContext.builder().build(),
                                                    REGISTRY_NAME,
                                                    String.format("%s:%s:%s", updatedGroup, updatedName, updatedVersion),
                                                    updateComponentArgs);
    assertEquals(REGISTRY_NAME, component.getRegistryName());
    assertEquals(updatedGroup, component.getGroup());
    assertEquals(updatedName, component.getName());
    assertEquals(updatedVersion, component.getVersion());
    assertNotNull(component.getCreationDate());
    assertNotNull(component.getUpdateDate());
    assertNotEquals(component.getCreationDate(), component.getUpdateDate());
  }

  @Test
  public void updateComponentToExistingComponent() throws Exception {
    final String group = "group";
    final String firstName = "updateComponentToExistingComponent_first";
    final String secondName = "updateComponentToExistingComponent_second";
    final String version = "1.2.3";

    CreateComponentArgs createComponentArgs = CreateComponentArgs.builder()
      .registryName(REGISTRY_NAME)
      .group(group)
      .name(firstName)
      .version(version)
      .build();
    Component component = componentController.createComponent(RequestContext.builder().build(),
                                                              ResponseContext.builder().build(),
                                                              createComponentArgs);
    assertNotNull(component.getId());

    createComponentArgs = CreateComponentArgs.builder()
      .registryName(REGISTRY_NAME)
      .group(group)
      .name(secondName)
      .version(version)
      .build();
    component = componentController.createComponent(RequestContext.builder().build(),
                                                    ResponseContext.builder().build(),
                                                    createComponentArgs);
    assertNotNull(component.getId());
    final String secondComponentId = component.getId();

    final UpdateComponentArgs updateComponentArgs = UpdateComponentArgs.builder()
      .group(group)
      .name(firstName)
      .version(version)
      .build();

    assertThrows(ConflictException.class,
                 () -> componentController.updateComponent(RequestContext.builder().build(),
                                                           ResponseContext.builder().build(),
                                                           secondComponentId,
                                                           updateComponentArgs));
  }
}
