package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.itesoft.registree.dto.Resource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class DockerHostedRegistryResourceTest extends DockerHostedRegistryWithDatabaseTest {
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

    execute("docker", "login", "-u", USERNAME, "-p", PASSWORD, "localhost:8090");

    initWithEmbedImages();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] {};
  }

  @Override
  public String[] getDockerImagesToRemove() {
    return new String[] { "alpine", "alpine/curl" };
  }

  @Test
  public void listResources() throws Exception {
    List<Resource> resources =
      registryResourceClient.getRootResources(HOSTED_REGISTRY_NAME);
    assertEquals(2, resources.size());
    Resource resource = resources.get(0);
    assertEquals("blobs", resource.getName());
    assertEquals("blobs", resource.getPath());
    assertEquals(null, resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resource = resources.get(1);
    assertEquals("repositories", resource.getName());
    assertEquals("repositories", resource.getPath());
    assertEquals(null, resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "blobs");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("sha256", resource.getName());
    assertEquals("blobs/sha256", resource.getPath());
    assertEquals("blobs", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    final List<String> orderedBlobs = Arrays.asList(BLOBS);
    orderedBlobs.sort((first, second) -> first.compareTo(second));
    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "blobs/sha256");
    assertEquals(orderedBlobs.size(), resources.size());
    int index = 0;
    for (final String blob : orderedBlobs) {
      final String[] tab = blob.split(":");
      final String digest = tab[1];
      final String prefix = digest.substring(0, 2);
      resource = resources.get(index);
      assertEquals(prefix, resource.getName());
      assertEquals("blobs/sha256/" + prefix, resource.getPath());
      assertEquals("blobs/sha256", resource.getParentPath());
      assertEquals("directory", resource.getType());
      assertNull(resource.getRelativeDownloadPath());
      assertNull(resource.getComponentGav());
      assertNull(resource.getFilePath());

      final List<Resource> blobResources = registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "blobs/sha256/" + prefix);
      assertEquals(1, blobResources.size());
      final Resource blobResource = blobResources.get(0);
      assertEquals(digest, blobResource.getName());
      assertEquals("blobs/sha256/" + prefix + "/" + digest, blobResource.getPath());
      assertEquals("blobs/sha256/" + prefix, blobResource.getParentPath());
      assertEquals("file", blobResource.getType());
      assertEquals("v2/blobs/" + blob, blobResource.getRelativeDownloadPath());
      assertNull(resource.getComponentGav());
      assertEquals("v2/blobs/" + blob, blobResource.getFilePath());

      index++;
    }

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "repositories");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("alpine", resource.getName());
    assertEquals("repositories/alpine", resource.getPath());
    assertEquals("repositories", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "repositories/alpine");
    assertEquals(2, resources.size());
    resource = resources.get(0);
    assertEquals("_manifests", resource.getName());
    assertEquals("repositories/alpine/_manifests", resource.getPath());
    assertEquals("repositories/alpine", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resource = resources.get(1);
    assertEquals("curl", resource.getName());
    assertEquals("repositories/alpine/curl", resource.getPath());
    assertEquals("repositories/alpine", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "repositories/alpine/_manifests");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("tags", resource.getName());
    assertEquals("repositories/alpine/_manifests/tags", resource.getPath());
    assertEquals("repositories/alpine/_manifests", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "repositories/alpine/_manifests/tags");
    assertEquals(3, resources.size());
    resource = resources.get(0);
    assertEquals("ehe", resource.getName());
    assertEquals("repositories/alpine/_manifests/tags/ehe", resource.getPath());
    assertEquals("repositories/alpine/_manifests/tags", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("v2/repositories/alpine/manifests/ehe", resource.getRelativeDownloadPath());
    assertEquals("alpine:ehe", resource.getComponentGav());
    assertEquals("v2/repositories/alpine/_manifests/tags/ehe", resource.getFilePath());

    resource = resources.get(1);
    assertEquals("latest", resource.getName());
    assertEquals("repositories/alpine/_manifests/tags/latest", resource.getPath());
    assertEquals("repositories/alpine/_manifests/tags", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("v2/repositories/alpine/manifests/latest", resource.getRelativeDownloadPath());
    assertEquals("alpine:latest", resource.getComponentGav());
    assertEquals("v2/repositories/alpine/_manifests/tags/latest", resource.getFilePath());

    resource = resources.get(2);
    assertEquals("test", resource.getName());
    assertEquals("repositories/alpine/_manifests/tags/test", resource.getPath());
    assertEquals("repositories/alpine/_manifests/tags", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("v2/repositories/alpine/manifests/test", resource.getRelativeDownloadPath());
    assertEquals("alpine:test", resource.getComponentGav());
    assertEquals("v2/repositories/alpine/_manifests/tags/test", resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "repositories/alpine/curl/_manifests");
    assertEquals(1, resources.size());
    resource = resources.get(0);
    assertEquals("tags", resource.getName());
    assertEquals("repositories/alpine/curl/_manifests/tags", resource.getPath());
    assertEquals("repositories/alpine/curl/_manifests", resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME, "repositories/alpine/curl/_manifests/tags");
    assertEquals(2, resources.size());
    resource = resources.get(0);
    assertEquals("ehe", resource.getName());
    assertEquals("repositories/alpine/curl/_manifests/tags/ehe", resource.getPath());
    assertEquals("repositories/alpine/curl/_manifests/tags", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("v2/repositories/alpine/curl/manifests/ehe", resource.getRelativeDownloadPath());
    assertEquals("alpine/curl:ehe", resource.getComponentGav());
    assertEquals("v2/repositories/alpine/curl/_manifests/tags/ehe", resource.getFilePath());

    resource = resources.get(1);
    assertEquals("latest", resource.getName());
    assertEquals("repositories/alpine/curl/_manifests/tags/latest", resource.getPath());
    assertEquals("repositories/alpine/curl/_manifests/tags", resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals("v2/repositories/alpine/curl/manifests/latest", resource.getRelativeDownloadPath());
    assertEquals("alpine/curl:latest", resource.getComponentGav());
    assertEquals("v2/repositories/alpine/curl/_manifests/tags/latest", resource.getFilePath());
  }
}
