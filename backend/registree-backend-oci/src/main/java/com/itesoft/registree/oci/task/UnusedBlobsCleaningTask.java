package com.itesoft.registree.oci.task;

import static com.itesoft.registree.oci.storage.Constant.BLOBS_PATH;
import static com.itesoft.registree.oci.storage.Constant.DATA_FILE_NAME;
import static com.itesoft.registree.oci.storage.Constant.LAYERS_FOLDER_NAME;
import static com.itesoft.registree.oci.storage.Constant.LINK_FILE_NAME;
import static com.itesoft.registree.oci.storage.Constant.MANIFEST_FOLDER_NAME;
import static com.itesoft.registree.oci.storage.Constant.REPOSITORIES_PATH;
import static com.itesoft.registree.oci.storage.Constant.REVISIONS_FOLDER_NAME;
import static com.itesoft.registree.oci.storage.Constant.TAGS_FOLDER_NAME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.StorageCapableRegistry;
import com.itesoft.registree.oci.config.OciRegistries;
import com.itesoft.registree.oci.dto.json.BlobDto;
import com.itesoft.registree.oci.dto.json.ManifestDto;
import com.itesoft.registree.oci.storage.BlobStorage;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

@Component
public class UnusedBlobsCleaningTask {
  @Autowired
  private OciRegistries ociRegistries;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private BlobStorage blobStorage;

  @Autowired
  private StorageHelper storageHelper;

  @Scheduled(cron = "${oci.garbageCollecting.cron:-}")
  public void deleteUnusedBlobs() throws IOException {
    for (final Registry registry : ociRegistries.getRegistries()) {
      if (registry instanceof final StorageCapableRegistry storageCapableRegistry
        && storageCapableRegistry.isDoStore()) {
        cleanRegistry(registry);
      }
    }
  }

  private void cleanRegistry(final Registry registry) throws IOException {
    final String storagePath = storageHelper.getStoragePath(registry);
    final Path repositoriesPath = Paths.get(storagePath,
                                            REPOSITORIES_PATH);
    final List<Path> manifestsPaths =
      Files.find(repositoriesPath,
                 Integer.MAX_VALUE,
                 (file, basicFileAttributes) -> {
                   return MANIFEST_FOLDER_NAME.equals(file.getFileName().toString());
                 })
        .collect(Collectors.toList());

    final Set<String> allUsedShas = new HashSet<>();
    for (final Path manifestsPath : manifestsPaths) {
      cleanRepository(registry,
                      manifestsPath,
                      allUsedShas);
    }

    final Path blobsPath = Paths.get(storagePath,
                                     BLOBS_PATH);
    Files.find(blobsPath,
               Integer.MAX_VALUE,
               (file, basicFileAttributes) -> {
                 return DATA_FILE_NAME.equals(file.getFileName().toString())
                   && basicFileAttributes.isRegularFile();
               })
      .forEach(file -> {
        final Path parent = file.getParent();
        final String sha = parent.getParent().getParent().getFileName().toString() + ":" + parent.getFileName().toString();
        if (!allUsedShas.contains(sha)) {
          deleteRecursively(parent);
        }
      });

    final Path basePath = Paths.get(storagePath);
    Files.find(basePath,
               Integer.MAX_VALUE,
               (file, basicFileAttributes) -> {
                 return basicFileAttributes.isDirectory();
               })
      .forEach(f -> {
        try {
          deleteIfEmpty(basePath, f);
        } catch (final IOException exception) {
          throw new RuntimeException(exception.getMessage(), exception);
        }
      });
  }

  private void deleteIfEmpty(final Path baseFolder,
                             final Path folder)
    throws IOException {
    if (folder == null || baseFolder.equals(folder)) {
      return;
    }
    final boolean empty;
    try (Stream<Path> entries = Files.list(folder)) {
      empty = !entries.findFirst().isPresent();
    }
    if (empty) {
      Files.delete(folder);
      deleteIfEmpty(baseFolder, folder.getParent());
    }
  }

  private void cleanRepository(final Registry registry,
                               final Path manifestsPath,
                               final Set<String> allUsedShas)
    throws IOException {
    final List<Path> manifestShaFiles =
      Files.find(Paths.get(manifestsPath.toString(),
                           TAGS_FOLDER_NAME),
                 Integer.MAX_VALUE,
                 (file, basicFileAttributes) -> {
                   return LINK_FILE_NAME.equals(file.getFileName().toString())
                     && basicFileAttributes.isRegularFile();
                 })
        .collect(Collectors.toList());

    final Set<String> usedManifestShas = new HashSet<>();
    final Set<String> usedLayerShas = new HashSet<>();
    for (final Path manifestShaFile : manifestShaFiles) {
      final String manifestSha = Files.readString(manifestShaFile);
      usedManifestShas.add(manifestSha);
      final byte[] data = blobStorage.getBlobData(registry, manifestSha);
      final ManifestDto manifestDto = objectMapper.readValue(data, ManifestDto.class);
      addUsedBlob(usedLayerShas, manifestDto.getConfig());
      addUsedBlobs(usedLayerShas, manifestDto.getLayers());
    }
    allUsedShas.addAll(usedManifestShas);
    allUsedShas.addAll(usedLayerShas);

    deleteUnusedRevisions(manifestsPath, usedManifestShas);
    deleteUnusedLayers(manifestsPath, usedLayerShas);
  }

  private void deleteUnusedRevisions(final Path manifestsPath,
                                     final Set<String> usedManifestShas)
    throws IOException {
    Files.find(Paths.get(manifestsPath.toString(),
                         REVISIONS_FOLDER_NAME),
               Integer.MAX_VALUE,
               (file, basicFileAttributes) -> {
                 if (!LINK_FILE_NAME.equals(file.getFileName().toString())) {
                   return false;
                 }

                 final String sha = getSha(file);
                 return !usedManifestShas.contains(sha);
               })
      .forEach(file -> {
        deleteRecursively(file.getParent());
      });
  }

  private void deleteUnusedLayers(final Path manifestsPath,
                                  final Set<String> usedLayerShas)
    throws IOException {
    final Path layersPath = manifestsPath.resolveSibling(LAYERS_FOLDER_NAME);
    Files.find(layersPath,
               Integer.MAX_VALUE,
               (file, basicFileAttributes) -> {
                 if (!LINK_FILE_NAME.equals(file.getFileName().toString())) {
                   return false;
                 }

                 final String sha = getSha(file);
                 return !usedLayerShas.contains(sha);
               })
      .forEach(file -> {
        deleteRecursively(file.getParent());
      });
  }

  private void addUsedBlobs(final Set<String> usedBlobShas, final List<BlobDto> blobDtos) {
    for (final BlobDto blobDto : blobDtos) {
      addUsedBlob(usedBlobShas, blobDto);
    }
  }

  private void addUsedBlob(final Set<String> usedBlobShas, final BlobDto blobDto) {
    usedBlobShas.add(blobDto.getDigest());
  }

  private void deleteRecursively(final Path path) {
    try {
      FileSystemUtils.deleteRecursively(path);
    } catch (final IOException exception) {
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  private String getSha(final Path linkFile) {
    return linkFile.getParent().getParent().getFileName().toString() + ":" + linkFile.getParent().getFileName().toString();
  }
}
