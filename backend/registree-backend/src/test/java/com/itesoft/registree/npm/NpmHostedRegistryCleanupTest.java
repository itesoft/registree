package com.itesoft.registree.npm;

import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.ws.rs.WebApplicationException;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.Gav;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class NpmHostedRegistryCleanupTest extends NpmHostedRegistryWithDatabaseTest {
  private String npmrcPath;

  @BeforeAll
  public void createUserAndRoute() throws Exception {
    createAnonymousHostedReadWriteRoute();

    npmrcPath = createAnonymousNpmrc(HOSTED_REGISTRY_NAME);
  }

  @Test
  public void cleanup() throws Exception {
    npmPublish(npmrcPath, rpc250303Library);
    npmPublish(npmrcPath, rpc250304Library);

    assertPackageJson("@itesoft", "rpc", true);
    assertTgz("@itesoft", "rpc", "25.3.3-master", true);
    assertTgz("@itesoft", "rpc", "25.3.4-master", true);

    Gav gav =
      Gav.builder()
        .group("@itesoft")
        .name("rpc")
        .version("25.3.3-master")
        .build();
    Component component = componentClient.getComponent(HOSTED_REGISTRY_NAME, gav.toString());
    final String firstComponentId = component.getId();
    componentClient.deleteComponent(component.getId(),
                                    null);

    WebApplicationException exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> componentClient.getComponent(firstComponentId));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());
    assertComponentAndFiles("@itesoft", "rpc", "25.3.4-master", ANONYMOUS_USERNAME);
    assertPackageJson("@itesoft", "rpc", true);
    assertTgz("@itesoft", "rpc", "25.3.3-master", false);
    assertTgz("@itesoft", "rpc", "25.3.4-master", true);

    gav =
      Gav.builder()
        .group("@itesoft")
        .name("rpc")
        .version("25.3.4-master")
        .build();
    component = componentClient.getComponent(HOSTED_REGISTRY_NAME, gav.toString());
    final String secondComponentId = component.getId();
    componentClient.deleteComponent(component.getId(),
                                    null);

    exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> componentClient.getComponent(firstComponentId));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());
    exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> componentClient.getComponent(secondComponentId));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());
    assertPackageJson("@itesoft", "rpc", false);
    assertTgz("@itesoft", "rpc", "25.3.3-master", false);
    assertTgz("@itesoft", "rpc", "25.3.4-master", false);
  }

  private void assertPackageJson(final String scope,
                                 final String name,
                                 final boolean exists) {
    final Path packageJsonPath =
      Paths.get(registreeDataConfiguration.getRegistriesPath(),
                REGISTRY_FOLDER_NAME,
                scope + "/" + name + "/package.json");
    assertEquals(exists, Files.isRegularFile(packageJsonPath));
  }

  private void assertTgz(final String scope,
                         final String name,
                         final String version,
                         final boolean exists) {
    final Path tgzPath =
      Paths.get(registreeDataConfiguration.getRegistriesPath(),
                REGISTRY_FOLDER_NAME,
                scope + "/" + name + "/" + name + "-" + version + ".tgz");
    assertEquals(exists, Files.isRegularFile(tgzPath));
  }
}
