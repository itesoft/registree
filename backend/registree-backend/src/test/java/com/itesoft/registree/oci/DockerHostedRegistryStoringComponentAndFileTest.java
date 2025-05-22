package com.itesoft.registree.oci;

import static com.itesoft.registree.oci.storage.Constant.BLOB_PATH;
import static com.itesoft.registree.oci.storage.Constant.DATA_FILE_NAME;
import static com.itesoft.registree.oci.storage.OciDigestHelper.fromString;
import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.File;
import com.itesoft.registree.oci.dto.json.BlobDto;
import com.itesoft.registree.oci.dto.json.ManifestDto;
import com.itesoft.registree.oci.storage.Digest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DockerHostedRegistryStoringComponentAndFileTest extends DockerHostedRegistryWithDatabaseTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  @BeforeAll
  public void init() throws Exception {
    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/hosted",
                "rw");

    execute("docker", "login", "-u", USERNAME, "-p", PASSWORD, "localhost:8090");
  }

  @Override
  public String[] getDockerImagesToRemove() {
    return new String[] { "alpine", "alpine/curl" };
  }

  @Test
  public void nativeDockerPushAndPull() throws Exception {
    pushImage("alpine");
    assertComponentAndFiles("alpine", "latest");
  }

  @Test
  public void nativeDockerPushAndPullWithComplexName() throws Exception {
    pushImage("alpine/curl");
    assertComponentAndFiles("alpine/curl", "latest");
  }

  private void pushImage(final String name)
    throws Exception {
    execute("docker", "pull", name);
    execute("docker", "tag", name, "localhost:8090/" + name);
    execute("docker", "push", "localhost:8090/" + name);
    execute("docker", "rmi", name);
    execute("docker", "rmi", "localhost:8090/" + name);
    execute("docker", "pull", "localhost:8090/" + name);
    execute("docker", "rmi", "localhost:8090/" + name);
  }

  private void assertComponentAndFiles(final String name,
                                       final String tag)
    throws Exception {
    final Component component = componentClient.getComponent(HOSTED_REGISTRY_NAME, name + ":" + tag);
    assertEquals(HOSTED_REGISTRY_NAME, component.getRegistryName());
    assertNull(component.getGroup());
    assertEquals(name, component.getName());
    assertEquals(tag, component.getVersion());

    final String filter = "component.id==" + component.getId();
    final List<File> files =
      fileClient.searchFiles(filter, null, null, null);
    assertEquals(1, files.size());
    File file = files.get(0);
    assertEquals(HOSTED_REGISTRY_NAME, file.getRegistryName());
    assertEquals(component.getId(), file.getComponentId());
    assertEquals("application/vnd.docker.distribution.manifest.v2+json", file.getContentType());
    assertEquals("v2/repositories/" + name + "/_manifests/tags/" + tag, file.getPath());
    assertEquals(USERNAME, file.getUploader());

    file =
      fileClient.getFile(HOSTED_REGISTRY_NAME, "v2/repositories/" + name + "/_manifests/tags/" + tag);
    assertEquals(HOSTED_REGISTRY_NAME, file.getRegistryName());
    assertEquals(component.getId(), file.getComponentId());
    assertEquals("application/vnd.docker.distribution.manifest.v2+json", file.getContentType());
    assertEquals("v2/repositories/" + name + "/_manifests/tags/" + tag, file.getPath());
    assertEquals(USERNAME, file.getUploader());

    final Path latestLinkPath =
      Paths.get(registreeDataConfiguration.getRegistriesPath(),
                REGISTRY_FOLDER_NAME,
                "v2/repositories/" + name + "/_manifests/tags/" + tag + "/current/link");
    assertTrue(Files.isRegularFile(latestLinkPath));
    final String manifestSha = Files.readString(latestLinkPath);
    final String manifestPath = "v2/blobs/" + manifestSha;
    file =
      fileClient.getFile(HOSTED_REGISTRY_NAME, manifestPath);
    assertEquals(HOSTED_REGISTRY_NAME, file.getRegistryName());
    assertNull(file.getComponentId());
    assertEquals("application/vnd.docker.distribution.manifest.v2+json", file.getContentType());
    assertEquals(manifestPath, file.getPath());
    assertEquals(USERNAME, file.getUploader());

    assertFiles(manifestSha);

    componentClient.deleteComponent(component.getId(),
                                    null);

    final Path latestTagPath =
      Paths.get(registreeDataConfiguration.getRegistriesPath(),
                REGISTRY_FOLDER_NAME,
                "v2/repositories/" + name + "/_manifests/tags/" + tag);

    assertFalse(Files.exists(latestTagPath));
  }

  private void assertFiles(final String manifestSha) throws Exception {
    final Path manifestPath = getBlobPath(manifestSha);
    final ManifestDto manifestDto;
    try (InputStream inputStream = Files.newInputStream(manifestPath)) {
      manifestDto = objectMapper.readValue(inputStream, ManifestDto.class);
    }

    assertBlobFile(manifestDto.getConfig());
    final List<BlobDto> layers = manifestDto.getLayers();
    assertBlobFiles(layers);
  }

  private void assertBlobFiles(final List<BlobDto> blobDtos) throws IOException {
    for (final BlobDto blobDto : blobDtos) {
      assertBlobFile(blobDto);
    }
  }

  private void assertBlobFile(final BlobDto blobDto) throws IOException {
    final Path blobPath = getBlobPath(blobDto.getDigest());
    assertTrue(Files.isRegularFile(blobPath));
    assertEquals(blobDto.getSize(), Files.size(blobPath));
  }

  private Path getBlobPath(final String blobSha) {
    final Digest digest = fromString(blobSha);
    final String prefix = digest.getHex().substring(0, 2);
    return Paths.get(registreeDataConfiguration.getRegistriesPath(),
                     REGISTRY_FOLDER_NAME,
                     String.format(BLOB_PATH,
                                   digest.getAlgorithm(),
                                   prefix,
                                   digest.getHex(),
                                   DATA_FILE_NAME));
  }
}
