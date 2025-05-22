package com.itesoft.registree.raw;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RawHostedRegistryStoringComponentAndFileTest extends RawHostedRegistryWithDatabaseTest {
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  @BeforeAll
  public void init() throws Exception {
    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/hosted",
                "rw");
  }

  @Test
  public void checkComponentAndFile() throws Exception {
    publishThenGetAndCheckFile(exceptionLib,
                               HOSTED_REGISTRY_NAME,
                               "path/to/exception",
                               "exception.tgz",
                               null,
                               USERNAME,
                               PASSWORD);
    assertComponentAndFiles("path/to/exception", "exception.tgz", "application/octet-stream");

    publishThenGetAndCheckFile(alpineTar.getFile().toPath(),
                               HOSTED_REGISTRY_NAME,
                               "docker-images",
                               "alpine.tar",
                               "application/x-tar",
                               USERNAME,
                               PASSWORD);
    assertComponentAndFiles("docker-images", "alpine.tar", "application/x-tar");
  }

  private void assertComponentAndFiles(final String path,
                                       final String name,
                                       final String contentType)
    throws Exception {
    final String filePath = Paths.get(path, name).toString();

    final Component component = componentClient.getComponent(HOSTED_REGISTRY_NAME, filePath);
    assertEquals(HOSTED_REGISTRY_NAME, component.getRegistryName());
    assertNull(component.getGroup());
    assertEquals(filePath, component.getName());
    assertNull(component.getVersion());

    final String filter = "component.id==" + component.getId();
    final List<File> files =
      fileClient.searchFiles(filter, null, null, null);
    assertEquals(1, files.size());
    File file = files.get(0);
    assertEquals(HOSTED_REGISTRY_NAME, file.getRegistryName());
    assertEquals(component.getId(), file.getComponentId());
    assertEquals(contentType, file.getContentType());
    assertEquals(filePath, file.getPath());
    assertEquals(USERNAME, file.getUploader());

    file =
      fileClient.getFile(HOSTED_REGISTRY_NAME, filePath);
    assertEquals(HOSTED_REGISTRY_NAME, file.getRegistryName());
    assertEquals(component.getId(), file.getComponentId());
    assertEquals(contentType, file.getContentType());
    assertEquals(filePath, file.getPath());
    assertEquals(USERNAME, file.getUploader());

    final Path onDriveFile = Paths.get(registreeDataConfiguration.getRegistriesPath(), REGISTRY_FOLDER_NAME, filePath);
    assertTrue(Files.isRegularFile(onDriveFile));

    componentClient.deleteComponent(component.getId(),
                                    null);

    assertFalse(Files.isRegularFile(onDriveFile));
  }
}
