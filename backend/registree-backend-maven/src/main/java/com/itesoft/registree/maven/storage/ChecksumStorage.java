package com.itesoft.registree.maven.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.itesoft.registree.RandomHelper;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ChecksumStorage extends AbstractMavenStorage {
  @Autowired
  private StorageHelper storageHelper;

  public MavenFile getMetadataChecksumFile(final Registry registry,
                                           final String groupId,
                                           final String artifactId,
                                           final String fileName) {
    final Path metadataFilePath = getMetadataChecksumFilePath(registry, groupId, artifactId, fileName);
    if (!Files.isRegularFile(metadataFilePath)) {
      return null;
    }
    return MavenFile.builder()
      .path(metadataFilePath)
      .contentType(MediaType.TEXT_PLAIN_VALUE)
      .build();
  }

  public String getMetadataChecksum(final Registry registry,
                                    final String groupId,
                                    final String artifactId,
                                    final String fileName)
    throws IOException {
    final Path metadataFilePath = getMetadataChecksumFilePath(registry, groupId, artifactId, fileName);
    if (!Files.isRegularFile(metadataFilePath)) {
      return null;
    }
    return Files.readString(metadataFilePath);
  }

  public MavenFile getArtifactChecksumFile(final Registry registry,
                                           final String groupId,
                                           final String artifactId,
                                           final String version,
                                           final String fileName) {
    final Path artifactFilePath = getArtifactChecksumFilePath(registry, groupId, artifactId, version, fileName);
    if (!Files.isRegularFile(artifactFilePath)) {
      return null;
    }
    return MavenFile.builder()
      .path(artifactFilePath)
      .contentType(MediaType.TEXT_PLAIN_VALUE)
      .build();
  }

  public String getArtifactChecksum(final Registry registry,
                                    final String groupId,
                                    final String artifactId,
                                    final String version,
                                    final String fileName)
    throws IOException {
    final Path artifactFilePath = getArtifactChecksumFilePath(registry, groupId, artifactId, version, fileName);
    if (!Files.isRegularFile(artifactFilePath)) {
      return null;
    }
    return Files.readString(artifactFilePath);
  }

  public void publishMetadataChecksum(final Registry registry,
                                      final String groupId,
                                      final String artifactId,
                                      final String fileName,
                                      final InputStream inputStream)
    throws Exception {
    final Path metadataFilePath = getMetadataChecksumFilePath(registry, groupId, artifactId, fileName);
    storeFile(metadataFilePath,
              inputStream);
  }

  public void publishMetadataChecksum(final Registry registry,
                                      final String groupId,
                                      final String artifactId,
                                      final String fileName,
                                      final String checksum)
    throws Exception {
    final Path metadataFilePath = getMetadataChecksumFilePath(registry, groupId, artifactId, fileName);
    Files.writeString(metadataFilePath, checksum);
  }

  public void publishArtifactChecksum(final Registry registry,
                                      final String groupId,
                                      final String artifactId,
                                      final String version,
                                      final String fileName,
                                      final InputStream inputStream)
    throws Exception {
    final Path artifactFilePath = getArtifactChecksumFilePath(registry, groupId, artifactId, version, fileName);
    storeFile(artifactFilePath,
              inputStream);
  }

  public void publishArtifactChecksum(final Registry registry,
                                      final String groupId,
                                      final String artifactId,
                                      final String version,
                                      final String fileName,
                                      final String checksum)
    throws Exception {
    final Path artifactFilePath = getArtifactChecksumFilePath(registry, groupId, artifactId, version, fileName);
    Files.writeString(artifactFilePath, checksum);
  }

  private Path getMetadataChecksumFilePath(final Registry registry,
                                           final String groupId,
                                           final String artifactId,
                                           final String fileName) {
    final String groupPath = groupId.replace('.', '/');
    return Paths.get(storageHelper.getStoragePath(registry),
                     groupPath,
                     artifactId,
                     fileName);
  }

  private Path getArtifactChecksumFilePath(final Registry registry,
                                           final String groupId,
                                           final String artifactId,
                                           final String version,
                                           final String fileName) {
    return getArtifactChecksumFilePath(registry,
                                       groupId,
                                       artifactId,
                                       version,
                                       fileName,
                                       false);
  }

  private Path getArtifactChecksumFilePath(final Registry registry,
                                           final String groupId,
                                           final String artifactId,
                                           final String version,
                                           final String fileName,
                                           final boolean temp) {
    final String groupPath = groupId.replace('.', '/');
    final String actualFileName = fileName + (temp ? "." + RandomHelper.random(6) : "");
    return Paths.get(storageHelper.getStoragePath(registry),
                     groupPath,
                     artifactId,
                     version,
                     actualFileName);
  }
}
