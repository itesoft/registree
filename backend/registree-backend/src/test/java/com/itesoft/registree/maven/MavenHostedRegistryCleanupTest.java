package com.itesoft.registree.maven;

import static com.itesoft.registree.maven.config.MavenConstants.METADATA_FILE_NAME;

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

public class MavenHostedRegistryCleanupTest extends MavenHostedRegistryWithDatabaseTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @BeforeAll
  public void createUserAndRoute() throws Exception {
    createAnonymousHostedReadWriteRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishArtifactsCheckComponentAndFiles() throws Exception {
    final Path settingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);

    mvnDeploy(firstProject1_0_0Folder, settingsFile);
    mvnDeploy(firstProject1_1_0Folder, settingsFile);
    mvnDeploy(secondProject2_3_4Folder, settingsFile);

    assertMetadata("com.itesoft.registree.test",
                   FISRT_ARTIFACT_NAME,
                   true);
    assertMetadata("com.itesoft.registree.test",
                   SECOND_ARTIFACT_NAME,
                   true);
    assertFiles("com.itesoft.registree.test",
                FISRT_ARTIFACT_NAME,
                "1.0.0",
                true);
    assertFiles("com.itesoft.registree.test",
                FISRT_ARTIFACT_NAME,
                "1.1.0",
                true);
    assertFiles("com.itesoft.registree.test",
                SECOND_ARTIFACT_NAME,
                "2.3.4",
                true);

    Gav gav =
      Gav.builder()
        .group("com.itesoft.registree.test")
        .name(FISRT_ARTIFACT_NAME)
        .version("1.1.0")
        .build();
    Component component = componentClient.getComponent(HOSTED_REGISTRY_NAME, gav.toString());
    final String firstComponentId = component.getId();
    componentClient.deleteComponent(component.getId(),
                                    null);

    WebApplicationException exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> componentClient.getComponent(firstComponentId));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());

    assertMetadata("com.itesoft.registree.test",
                   FISRT_ARTIFACT_NAME,
                   true);
    assertMetadata("com.itesoft.registree.test",
                   SECOND_ARTIFACT_NAME,
                   true);
    assertFiles("com.itesoft.registree.test",
                FISRT_ARTIFACT_NAME,
                "1.0.0",
                true);
    assertFiles("com.itesoft.registree.test",
                FISRT_ARTIFACT_NAME,
                "1.1.0",
                false);
    assertFiles("com.itesoft.registree.test",
                SECOND_ARTIFACT_NAME,
                "2.3.4",
                true);

    removeMavenArtifactsFromLocalRepository();
    mvnInstall(settingsFile,
               useFirstProjectFolder.toFile());

    gav =
      Gav.builder()
        .group("com.itesoft.registree.test")
        .name(FISRT_ARTIFACT_NAME)
        .version("1.0.0")
        .build();
    component = componentClient.getComponent(HOSTED_REGISTRY_NAME, gav.toString());
    final String secondComponentId = component.getId();
    componentClient.deleteComponent(component.getId(),
                                    null);
    exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> componentClient.getComponent(secondComponentId));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());

    assertMetadata("com.itesoft.registree.test",
                   FISRT_ARTIFACT_NAME,
                   false);
    assertMetadata("com.itesoft.registree.test",
                   SECOND_ARTIFACT_NAME,
                   true);
    assertFiles("com.itesoft.registree.test",
                FISRT_ARTIFACT_NAME,
                "1.0.0",
                false);
    assertFiles("com.itesoft.registree.test",
                FISRT_ARTIFACT_NAME,
                "1.1.0",
                false);
    assertFiles("com.itesoft.registree.test",
                SECOND_ARTIFACT_NAME,
                "2.3.4",
                true);

    gav =
      Gav.builder()
        .group("com.itesoft.registree.test")
        .name(SECOND_ARTIFACT_NAME)
        .version("2.3.4")
        .build();
    component = componentClient.getComponent(HOSTED_REGISTRY_NAME, gav.toString());
    final String thirdComponentId = component.getId();
    componentClient.deleteComponent(component.getId(),
                                    null);
    exceptionThrow =
      assertThrows(WebApplicationException.class,
                   () -> componentClient.getComponent(thirdComponentId));
    assertEquals(HttpStatus.NOT_FOUND.value(), exceptionThrow.getResponse().getStatus());

    assertMetadata("com.itesoft.registree.test",
                   FISRT_ARTIFACT_NAME,
                   false);
    assertMetadata("com.itesoft.registree.test",
                   SECOND_ARTIFACT_NAME,
                   false);
    assertFiles("com.itesoft.registree.test",
                FISRT_ARTIFACT_NAME,
                "1.0.0",
                false);
    assertFiles("com.itesoft.registree.test",
                FISRT_ARTIFACT_NAME,
                "1.1.0",
                false);
    assertFiles("com.itesoft.registree.test",
                SECOND_ARTIFACT_NAME,
                "2.3.4",
                false);
  }

  private void assertMetadata(final String groupId,
                              final String artifactId,
                              final boolean exists) {
    final Path metadataPath =
      Paths.get(registreeDataConfiguration.getRegistriesPath(),
                REGISTRY_FOLDER_NAME,
                Paths.get(groupId.replace('.', '/'), artifactId, METADATA_FILE_NAME).toString());
    assertEquals(exists, Files.isRegularFile(metadataPath));
  }

  private void assertFiles(final String groupId,
                           final String artifactId,
                           final String version,
                           final boolean exists) {
    for (final String extension : new String[] { "jar", "jar.md5", "jar.sha1", "pom", "pom.md5", "pom.sha1" }) {
      final Path filePath =
        Paths.get(registreeDataConfiguration.getRegistriesPath(),
                  REGISTRY_FOLDER_NAME,
                  Paths.get(groupId.replace('.', '/'), artifactId, version, artifactId + "-" + version + "." + extension).toString());
      assertEquals(exists,
                   Files.isRegularFile(filePath),
                   () -> "Expecting file [" + filePath + "] to " + (exists ? "" : "not ") + "exist");
    }
  }
}
