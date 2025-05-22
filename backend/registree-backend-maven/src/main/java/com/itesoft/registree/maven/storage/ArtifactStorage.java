package com.itesoft.registree.maven.storage;

import static com.itesoft.registree.IoHelper.closeSilently;
import static com.itesoft.registree.IoHelper.deleteSilently;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.itesoft.registree.RandomHelper;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.maven.api.MavenApiCallback;
import com.itesoft.registree.maven.dto.ArtifactCreation;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

@Component
public class ArtifactStorage extends AbstractMavenStorage {
  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private MetadataStorage metadataStorage;

  @Autowired
  private MavenApiCallback apiCallback;

  public MavenFile getArtifactFile(final Registry registry,
                                   final String groupId,
                                   final String artifactId,
                                   final String version,
                                   final String fileName)
    throws Exception {
    final Path artifactFilePath = getArtifactFilePath(registry, groupId, artifactId, version, fileName);
    if (!Files.isRegularFile(artifactFilePath)) {
      return null;
    }
    final String contentType = getContentType(fileName);
    return MavenFile.builder()
      .path(artifactFilePath)
      .contentType(contentType)
      .build();
  }

  public void publishArtifactFile(final Registry registry,
                                  final String groupId,
                                  final String artifactId,
                                  final String version,
                                  final String fileName,
                                  final InputStream inputStream)
    throws Exception {
    final Path artifactFilePath = getArtifactFilePath(registry, groupId, artifactId, version, fileName);
    final Path tempArtifacttFilePath = getArtifactFilePath(registry, groupId, artifactId, version, fileName, true);
    storeFile(artifactFilePath,
              tempArtifacttFilePath,
              inputStream);

    final Path relativePath = relativize(registry, artifactFilePath);
    final String contentType = getContentType(fileName);
    createOrUpdateComponentAndFile(registry,
                                   groupId,
                                   artifactId,
                                   version,
                                   relativePath.toString(),
                                   contentType);
  }

  public void prepareArtifactCreation(final Registry registry,
                                      final String groupId,
                                      final String artifactId,
                                      final String version,
                                      final String fileName)
    throws IOException {
    final Path artifactFilePath = getArtifactFilePath(registry, groupId, artifactId, version, fileName);
    final Path relativePath = relativize(registry, artifactFilePath);
    final String contentType = getContentType(fileName);

    createOrUpdateComponentAndFile(registry,
                                   groupId,
                                   artifactId,
                                   version,
                                   relativePath.toString(),
                                   contentType);
  }

  public ArtifactCreation initiateArtifactCreation(final Registry registry,
                                                   final String groupId,
                                                   final String artifactId,
                                                   final String version,
                                                   final String fileName)
    throws IOException {
    final Path artifactPath = getArtifactFilePath(registry, groupId, artifactId, version, fileName);
    Files.createDirectories(artifactPath.getParent());

    final Path tempArtifactPath = getArtifactFilePath(registry, groupId, artifactId, version, fileName, true);
    final OutputStream outputStream = Files.newOutputStream(tempArtifactPath);

    return ArtifactCreation.builder()
      .tarballPath(artifactPath)
      .tempTarballPath(tempArtifactPath)
      .outputStream(outputStream)
      .build();
  }

  public void createArtifact(final Registry registry,
                             final ArtifactCreation artifactCreation)
    throws IOException {
    closeSilently(artifactCreation.getOutputStream());

    Files.move(artifactCreation.getTempTarballPath(),
               artifactCreation.getTarballPath(),
               StandardCopyOption.ATOMIC_MOVE,
               StandardCopyOption.REPLACE_EXISTING);
  }

  public void abortArtifactCreation(final Registry registry,
                                    final ArtifactCreation artifactCreation) {
    closeSilently(artifactCreation.getOutputStream());
    deleteSilently(artifactCreation.getTempTarballPath());
  }

  private void createOrUpdateComponentAndFile(final Registry registry,
                                              final String groupId,
                                              final String artifactId,
                                              final String version,
                                              final String filePath,
                                              final String contentType) {
    final String actualContentType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;
    boolean exists = apiCallback.componentExists(registry,
                                                 groupId,
                                                 artifactId,
                                                 version);
    final String componentId;
    if (exists) {
      componentId =
        apiCallback.updateComponent(registry,
                                    groupId,
                                    artifactId,
                                    version);
    } else {
      componentId =
        apiCallback.createComponent(registry,
                                    groupId,
                                    artifactId,
                                    version);
    }

    exists = apiCallback.fileExists(registry,
                                    filePath);
    if (exists) {
      apiCallback.updateFile(registry,
                             componentId,
                             filePath,
                             actualContentType);
    } else {
      apiCallback.createFile(registry,
                             componentId,
                             filePath,
                             actualContentType);
    }
  }

  public void deleteArtifact(final Registry registry,
                             final String groupId,
                             final String artifactId,
                             final String version)
    throws Exception {
    final Path artifactFolder = getArtifactFolderPath(registry, groupId, artifactId, version);
    if (!Files.isDirectory(artifactFolder)) {
      return;
    }
    FileSystemUtils.deleteRecursively(artifactFolder);
    metadataStorage.removeVersionFromMetadata(registry, groupId, artifactId, version);
  }

  private Path getArtifactFolderPath(final Registry registry,
                                     final String groupId,
                                     final String artifactId,
                                     final String version) {
    final String groupPath = groupId.replace('.', '/');
    return Paths.get(storageHelper.getStoragePath(registry),
                     groupPath,
                     artifactId,
                     version);
  }

  private Path getArtifactFilePath(final Registry registry,
                                   final String groupId,
                                   final String artifactId,
                                   final String version,
                                   final String fileName) {
    return getArtifactFilePath(registry,
                               groupId,
                               artifactId,
                               version,
                               fileName,
                               false);
  }

  private Path getArtifactFilePath(final Registry registry,
                                   final String groupId,
                                   final String artifactId,
                                   final String version,
                                   final String fileName,
                                   final boolean temp) {
    final Path folderPath = getArtifactFolderPath(registry, groupId, artifactId, version);
    final String actualFileName = fileName + (temp ? "." + RandomHelper.random(6) : "");
    return Paths.get(folderPath.toString(),
                     actualFileName);
  }
}
