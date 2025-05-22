package com.itesoft.registree.oci.storage;

import static com.itesoft.registree.oci.storage.Constant.LAYER_PATH;
import static com.itesoft.registree.oci.storage.Constant.MANIFESTS_PATH;
import static com.itesoft.registree.oci.storage.Constant.MANIFEST_REVISION_PATH;
import static com.itesoft.registree.oci.storage.Constant.MANIFEST_TAG_CURRENT_PATH;
import static com.itesoft.registree.oci.storage.Constant.MANIFEST_TAG_INDEX_PATH;
import static com.itesoft.registree.oci.storage.Constant.MANIFEST_TAG_PATH;
import static com.itesoft.registree.oci.storage.Constant.REPOSITORIES_PATH;
import static com.itesoft.registree.oci.storage.Constant.REPOSITORY_TAG_FILE_PATH;
import static com.itesoft.registree.oci.storage.Constant.TAGS_PATH;
import static com.itesoft.registree.oci.storage.OciDigestHelper.fromString;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.oci.api.OciApiCallback;
import com.itesoft.registree.oci.dto.Blob;
import com.itesoft.registree.oci.dto.Manifest;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

@Service
public class RepositoryStorage {
  private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryStorage.class);

  @Autowired
  private OciApiCallback ociApiCallback;

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private BlobStorage blobStorage;

  public List<String> getRepositories(final Registry registry)
    throws IOException {
    final Path repositoriesPath = getRepositoriesPath(registry);
    if (!Files.isDirectory(repositoriesPath)) {
      return null;
    }

    final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/_manifests");
    return Files.walk(repositoriesPath)
      .filter(pathMatcher::matches)
      .map(path -> repositoriesPath.relativize(path.getParent()).toString())
      .collect(Collectors.toList());
  }

  public List<String> getTags(final Registry registry,
                              final String name) {
    final Path tagsPath = getTagsPath(registry, name);
    if (!Files.isDirectory(tagsPath)) {
      return null;
    }
    try (Stream<Path> paths = Files.list(tagsPath)) {
      return paths.filter(path -> Files.isDirectory(path))
        .map(path -> path.getFileName().toString())
        .collect(Collectors.toList());
    } catch (final IOException exception) {
      LOGGER.error(exception.getMessage(), exception);
      return null;
    }
  }

  public boolean repositoryExists(final Registry registry,
                                  final String name) {
    final Path repositoryManifestsPath = Paths.get(storageHelper.getStoragePath(registry),
                                                   MANIFESTS_PATH);
    return Files.isDirectory(repositoryManifestsPath);
  }

  public String getLayerDigest(final Registry registry,
                               final String name,
                               final String digest)
    throws IOException {
    final Path layerPath = getLayerPath(registry, name, digest);
    if (!Files.exists(layerPath)) {
      return null;
    }
    return Files.readString(layerPath);
  }

  public void createLayer(final Registry registry,
                          final String name,
                          final String digest)
    throws IOException {
    final Path layerPath = getLayerPath(registry, name, digest);
    writeDigest(layerPath, digest.getBytes());
  }

  public Manifest getManifest(final Registry registry,
                              final String name,
                              final String tag,
                              final String digest,
                              final boolean withData)
    throws IOException {
    final Path manifestPath;
    if (tag == null) {
      manifestPath = getManifestRevisionPath(registry, name, digest);
    } else if (digest == null) {
      manifestPath = getManifestTagCurrentPath(registry, name, tag);
    } else {
      manifestPath = getManifestTagIndexPath(registry, name, tag, digest);
    }

    if (!Files.isRegularFile(manifestPath)) {
      return null;
    }

    final String blobDigest = Files.readString(manifestPath);
    final Blob blob = blobStorage.getBlob(registry, blobDigest);
    if (blob == null) {
      return null;
    }
    byte[] data = null;
    if (withData) {
      data = blobStorage.getBlobData(registry, blobDigest);
    }

    return new Manifest(blobDigest, blob.getContentType(), blob.getContentLength(), data);
  }

  public void createManifest(final Registry registry,
                             final String name,
                             final String tag,
                             final String digest,
                             final String type,
                             final byte[] manifest)
    throws IOException {
    blobStorage.createBlob(registry, digest, type, manifest);

    final byte[] digestAsBytes = digest.getBytes();

    if (digest != null) {
      final Path revisionPath = getManifestRevisionPath(registry, name, digest);
      writeDigest(revisionPath, digestAsBytes);
    }

    if (tag != null) {
      final Path tagCurrentPath = getManifestTagCurrentPath(registry, name, tag);
      writeDigest(tagCurrentPath, digestAsBytes);

      final boolean exists = ociApiCallback.componentExists(registry,
                                                            name,
                                                            tag);
      if (!exists) {
        final String componentId =
          ociApiCallback.createComponent(registry,
                                         name,
                                         tag);
        ociApiCallback.createFile(registry,
                                  componentId,
                                  String.format(REPOSITORY_TAG_FILE_PATH, name, tag),
                                  type);
      } else {
        final String componentId =
          ociApiCallback.updateComponent(registry,
                                         name,
                                         tag);
        ociApiCallback.updateFile(registry,
                                  componentId,
                                  String.format(REPOSITORY_TAG_FILE_PATH, name, tag),
                                  type);
      }
    }

    if (digest != null && tag != null) {
      final Path tagIndexPath = getManifestTagIndexPath(registry, name, tag, digest);
      writeDigest(tagIndexPath, digestAsBytes);
    }
  }

  public void deleteManifest(final Registry registry,
                             final String name,
                             final String tag)
    throws Exception {
    final Path manifestTagPath = getManifestTagPath(registry,
                                                    name,
                                                    tag);
    FileSystemUtils.deleteRecursively(manifestTagPath);
  }

  private void writeDigest(final Path path,
                           final byte[] digest)
    throws IOException {
    Files.createDirectories(path.getParent());
    Files.write(path, digest);
  }

  private Path getRepositoriesPath(final Registry registry) {
    return Paths.get(storageHelper.getStoragePath(registry),
                     REPOSITORIES_PATH);
  }

  private Path getTagsPath(final Registry registry,
                           final String name) {
    final String relativePath = String.format(TAGS_PATH,
                                              name);
    return Paths.get(storageHelper.getStoragePath(registry),
                     relativePath);
  }

  private Path getLayerPath(final Registry registry,
                            final String name,
                            final String digest) {
    final Digest digestObj = fromString(digest);
    final String relativePath = String.format(LAYER_PATH,
                                              name,
                                              digestObj.getAlgorithm(),
                                              digestObj.getHex());
    return Paths.get(storageHelper.getStoragePath(registry),
                     relativePath);
  }

  private Path getManifestRevisionPath(final Registry registry,
                                       final String name,
                                       final String digest) {
    final Digest digestObj = fromString(digest);
    final String relativePath = String.format(MANIFEST_REVISION_PATH,
                                              name,
                                              digestObj.getAlgorithm(),
                                              digestObj.getHex());
    return Paths.get(storageHelper.getStoragePath(registry),
                     relativePath);
  }

  private Path getManifestTagPath(final Registry registry,
                                  final String name,
                                  final String tag) {
    final String relativePath = String.format(MANIFEST_TAG_PATH,
                                              name,
                                              tag);
    return Paths.get(storageHelper.getStoragePath(registry),
                     relativePath);
  }

  private Path getManifestTagCurrentPath(final Registry registry,
                                         final String name,
                                         final String tag) {
    final String relativePath = String.format(MANIFEST_TAG_CURRENT_PATH,
                                              name,
                                              tag);
    return Paths.get(storageHelper.getStoragePath(registry),
                     relativePath);
  }

  private Path getManifestTagIndexPath(final Registry registry,
                                       final String name,
                                       final String tag,
                                       final String digest) {
    final Digest digestObj = fromString(digest);
    final String relativePath = String.format(MANIFEST_TAG_INDEX_PATH,
                                              name,
                                              tag,
                                              digestObj.getAlgorithm(),
                                              digestObj.getHex());
    return Paths.get(storageHelper.getStoragePath(registry),
                     relativePath);
  }
}
