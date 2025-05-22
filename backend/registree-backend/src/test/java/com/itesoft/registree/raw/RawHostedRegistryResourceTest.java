package com.itesoft.registree.raw;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.List;

import com.itesoft.registree.dto.Resource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class RawHostedRegistryResourceTest extends RawHostedRegistryWithDatabaseTest {
  protected static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  @BeforeAll
  public void init() throws Exception {
    FileSystemUtils.deleteRecursively(Paths.get(registreeDataConfiguration.getRegistriesPath(), REGISTRY_FOLDER_NAME));

    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/hosted",
                "rw");

    publishThenGetAndCheckFile(exceptionLib,
                               HOSTED_REGISTRY_NAME,
                               "path/to/exception",
                               "exception.tgz",
                               null,
                               USERNAME,
                               PASSWORD);
    publishThenGetAndCheckFile(alpineTar.getFile().toPath(),
                               HOSTED_REGISTRY_NAME,
                               "docker/images",
                               "alpine.tar",
                               null,
                               USERNAME,
                               PASSWORD);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] {};
  }

  @Test
  public void listResources() throws Exception {
    List<Resource> resources =
      registryResourceClient.getRootResources(HOSTED_REGISTRY_NAME);
    assertEquals(2, resources.size());
    Resource resource = resources.get(0);
    assertEquals("docker", resource.getName());
    assertEquals("docker", resource.getPath());
    assertEquals(null, resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resource = resources.get(1);
    assertEquals("path", resource.getName());
    assertEquals("path", resource.getPath());
    assertEquals(null, resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "docker");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("images", resource.getName());
    assertEquals("docker/images", resource.getPath());
    assertEquals("docker", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "docker/images");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("alpine.tar", resource.getName());
    assertEquals("docker/images/alpine.tar", resource.getPath());
    assertEquals("docker/images", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("docker/images/alpine.tar", resource.getRelativeDownloadPath());
    assertEquals("docker/images/alpine.tar", resource.getComponentGav());
    assertEquals("docker/images/alpine.tar", resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "path");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("to", resource.getName());
    assertEquals("path/to", resource.getPath());
    assertEquals("path", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "path/to");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("exception", resource.getName());
    assertEquals("path/to/exception", resource.getPath());
    assertEquals("path/to", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "path/to/exception");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("exception.tgz", resource.getName());
    assertEquals("path/to/exception/exception.tgz", resource.getPath());
    assertEquals("path/to/exception", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("path/to/exception/exception.tgz", resource.getRelativeDownloadPath());
    assertEquals("path/to/exception/exception.tgz", resource.getComponentGav());
    assertEquals("path/to/exception/exception.tgz", resource.getFilePath());
  }
}
