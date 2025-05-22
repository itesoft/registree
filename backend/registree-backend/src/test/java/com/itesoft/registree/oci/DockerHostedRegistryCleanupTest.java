package com.itesoft.registree.oci;

import static com.itesoft.registree.oci.storage.Constant.BLOB_PATH;
import static com.itesoft.registree.oci.storage.Constant.DATA_FILE_NAME;
import static com.itesoft.registree.oci.storage.Constant.LAYER_PATH;
import static com.itesoft.registree.oci.storage.Constant.MANIFEST_REVISION_PATH;
import static com.itesoft.registree.oci.storage.OciDigestHelper.fromString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.oci.dto.json.BlobDto;
import com.itesoft.registree.oci.dto.json.ManifestDto;
import com.itesoft.registree.oci.storage.Digest;
import com.itesoft.registree.oci.task.UnusedBlobsCleaningTask;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;

public class DockerHostedRegistryCleanupTest extends DockerHostedRegistryWithDatabaseTest {
  @FunctionalInterface
  public static interface Validator {
    void test() throws Exception;
  }

  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @Autowired
  private UnusedBlobsCleaningTask unusedBlobsCleaningTask;

  private ManifestDto alpineLatestManifest;
  private ManifestDto alpineEheManifest;
  private ManifestDto alpineTestManifest;
  private ManifestDto alpineCurlLatestManifest;
  private ManifestDto alpineCurlEheManifest;

  @BeforeAll
  public void init() throws Exception {
    FileSystemUtils.deleteRecursively(Paths.get(registreeDataConfiguration.getRegistriesPath(), REGISTRY_FOLDER_NAME));

    createAnonymousHostedReadWriteRoute();

    initWithEmbedImages();

    alpineLatestManifest = getManifest("alpine", "latest");
    alpineEheManifest = getManifest("alpine", "ehe");
    alpineTestManifest = getManifest("alpine", "test");
    alpineCurlLatestManifest = getManifest("alpine/curl", "latest");
    alpineCurlEheManifest = getManifest("alpine/curl", "ehe");
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
  public void cleanup() throws Exception {
    final Validator allBlobsExistValidator = () -> {
      assertBlobFiles(alpineLatestManifest, "alpine", true);
      assertBlobFiles(alpineEheManifest, "alpine", true);
      assertBlobFiles(alpineTestManifest, "alpine", true);
      assertBlobFiles(alpineCurlLatestManifest, "alpine/curl", true);
      assertBlobFiles(alpineCurlEheManifest, "alpine/curl", true);
    };

    final Validator alpineDeletedValidator = () -> {
      assertBlobFile(ALPINE_MANIFEST_SHA, false);
      assertRevisionFile("alpine", ALPINE_MANIFEST_SHA, false);

      assertBlobFile(ALPINE_CONFIG_SHA, false);
      assertLayerFile("alpine", ALPINE_CONFIG_SHA, false);

      assertBlobFile(ALPINE_ALPINE_CURL_SHARED_LAYER_SHA, true);
      assertLayerFile("alpine", ALPINE_ALPINE_CURL_SHARED_LAYER_SHA, false);
      assertLayerFile("alpine/curl", ALPINE_ALPINE_CURL_SHARED_LAYER_SHA, true);

      assertBlobFile(ALPINE_CURL_CONFIG_SHA, true);
      assertLayerFile("alpine/curl", ALPINE_CURL_CONFIG_SHA, true);

      assertBlobFiles(ALPINE_CURL_LAYERS_SHA, true);
      assertLayerFiles("alpine/curl", ALPINE_CURL_LAYERS_SHA, true);
    };

    deleteComponent("alpine", "latest");
    allBlobsExistValidator.test();
    unusedBlobsCleaningTask.deleteUnusedBlobs();
    allBlobsExistValidator.test();

    deleteComponent("alpine", "test");
    allBlobsExistValidator.test();
    unusedBlobsCleaningTask.deleteUnusedBlobs();
    allBlobsExistValidator.test();

    deleteComponent("alpine", "ehe");
    allBlobsExistValidator.test();
    unusedBlobsCleaningTask.deleteUnusedBlobs();
    alpineDeletedValidator.test();

    deleteComponent("alpine/curl", "latest");
    alpineDeletedValidator.test();
    unusedBlobsCleaningTask.deleteUnusedBlobs();
    alpineDeletedValidator.test();

    deleteComponent("alpine/curl", "ehe");
    alpineDeletedValidator.test();
    unusedBlobsCleaningTask.deleteUnusedBlobs();

    assertBlobFiles(alpineLatestManifest, "alpine", false);
    assertBlobFiles(alpineEheManifest, "alpine", false);
    assertBlobFiles(alpineTestManifest, "alpine", false);
    assertBlobFiles(alpineCurlLatestManifest, "alpine/curl", false);
    assertBlobFiles(alpineCurlEheManifest, "alpine/curl", false);
  }

  private ManifestDto getManifest(final String name,
                                  final String tag)
    throws Exception {
    final Path latestLinkPath =
      Paths.get(registreeDataConfiguration.getRegistriesPath(),
                REGISTRY_FOLDER_NAME,
                "v2/repositories/" + name + "/_manifests/tags/" + tag + "/current/link");
    final String manifestSha = Files.readString(latestLinkPath);
    final Path manifestPath = getBlobPath(manifestSha);
    try (InputStream inputStream = Files.newInputStream(manifestPath)) {
      return objectMapper.readValue(inputStream, ManifestDto.class);
    }
  }

  private void deleteComponent(final String name,
                               final String tag) {
    final Component component = componentClient.getComponent(HOSTED_REGISTRY_NAME, name + ":" + tag);
    componentClient.deleteComponent(component.getId(),
                                    null);
  }

  private void assertBlobFiles(final ManifestDto manifest,
                               final String name,
                               final boolean exists)
    throws Exception {
    assertBlobFile(manifest.getConfig(), exists);
    final List<BlobDto> layers = manifest.getLayers();
    assertBlobFiles(layers, exists);
    assertLayerFiles(name, layers, exists);
  }

  private void assertBlobFiles(final List<BlobDto> blobDtos,
                               final boolean exists)
    throws IOException {
    for (final BlobDto blobDto : blobDtos) {
      assertBlobFile(blobDto, exists);
    }
  }

  private void assertBlobFile(final BlobDto blobDto,
                              final boolean exists)
    throws IOException {
    assertBlobFile(blobDto.getDigest(), blobDto.getSize(), exists);
  }

  private void assertBlobFiles(final String[] blobShas,
                               final boolean exists)
    throws IOException {
    for (final String blobSha : blobShas) {
      assertBlobFile(blobSha, -1, exists);
    }
  }

  private void assertBlobFile(final String blobSha,
                              final boolean exists)
    throws IOException {
    assertBlobFile(blobSha, -1, exists);
  }

  private void assertBlobFile(final String blobSha,
                              final long size,
                              final boolean exists)
    throws IOException {
    final Path blobPath = getBlobPath(blobSha);
    assertFile(blobPath, size, exists);
  }

  private void assertLayerFiles(final String name,
                                final List<BlobDto> layers,
                                final boolean exists)
    throws IOException {
    for (final BlobDto blobDto : layers) {
      assertLayerFile(name, blobDto, exists);
    }
  }

  private void assertLayerFiles(final String name,
                                final String[] blobShas,
                                final boolean exists)
    throws IOException {
    for (final String blobSha : blobShas) {
      assertLayerFile(name, blobSha, exists);
    }
  }

  private void assertLayerFile(final String name,
                               final BlobDto layer,
                               final boolean exists)
    throws IOException {
    assertLayerFile(name, layer.getDigest(), exists);
  }

  private void assertLayerFile(final String name,
                               final String layerSha,
                               final boolean exists)
    throws IOException {
    final Path layerPath = getLayerPath(name, layerSha);
    assertFile(layerPath, -1, exists);
  }

  private void assertRevisionFile(final String name,
                                  final String layerSha,
                                  final boolean exists)
    throws IOException {
    final Path revisionPath = getRevisionPath(name, layerSha);
    assertFile(revisionPath, -1, exists);
  }

  private void assertFile(final Path path,
                          final long size,
                          final boolean exists)
    throws IOException {
    if (exists) {
      assertTrue(Files.isRegularFile(path),
                 "File " + path + " should exist");
      if (size != -1) {
        assertEquals(size, Files.size(path));
      }
    } else {
      assertFalse(Files.exists(path),
                  "File " + path + " should not exist");
    }
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

  private Path getLayerPath(final String name,
                            final String blobSha) {
    final Digest digest = fromString(blobSha);
    return Paths.get(registreeDataConfiguration.getRegistriesPath(),
                     REGISTRY_FOLDER_NAME,
                     String.format(LAYER_PATH,
                                   name,
                                   digest.getAlgorithm(),
                                   digest.getHex()));
  }

  private Path getRevisionPath(final String name,
                               final String blobSha) {
    final Digest digest = fromString(blobSha);
    return Paths.get(registreeDataConfiguration.getRegistriesPath(),
                     REGISTRY_FOLDER_NAME,
                     String.format(MANIFEST_REVISION_PATH,
                                   name,
                                   digest.getAlgorithm(),
                                   digest.getHex()));
  }
}
