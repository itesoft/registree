package com.itesoft.registree.maven.storage;

import static com.itesoft.registree.IoHelper.closeSilently;
import static com.itesoft.registree.IoHelper.deleteSilently;
import static com.itesoft.registree.java.DigestHelper.bytesToHex;
import static com.itesoft.registree.maven.config.MavenConstants.METADATA_FILE_NAME;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.itesoft.registree.RandomHelper;
import com.itesoft.registree.configuration.RegistreeXmlConfiguration;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.maven.dto.MetadataCreation;
import com.itesoft.registree.maven.dto.xml.MavenMetadata;
import com.itesoft.registree.maven.dto.xml.MavenMetadataVersioning;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class MetadataStorage extends AbstractMavenStorage {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataStorage.class);

  @Autowired
  private RegistreeXmlConfiguration registreeXmlConfiguration;

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private ChecksumStorage checksumStorage;

  public MavenFile getMetadataFile(final Registry registry,
                                   final String groupId,
                                   final String artifactId) {
    final Path metadataFilePath = getMetadataFilePath(registry, groupId, artifactId);
    if (!Files.isRegularFile(metadataFilePath)) {
      return null;
    }
    return MavenFile.builder()
      .path(metadataFilePath)
      .contentType(MediaType.APPLICATION_XML_VALUE)
      .build();
  }

  public void publishMetadataFile(final Registry registry,
                                  final String groupId,
                                  final String artifactId,
                                  final InputStream inputStream)
    throws Exception {
    final MavenMetadata givenMetadata = registreeXmlConfiguration.getXmlMapper().readValue(inputStream, MavenMetadata.class);

    MavenMetadata newMetadata = loadExistingMetadata(registry, groupId, artifactId);
    if (newMetadata == null) {
      newMetadata = givenMetadata;
    } else {
      newMetadata.getVersioning().setLastUpdated(newMetadata.getVersioning().getLastUpdated());
      final List<String> versions = new ArrayList<>(newMetadata.getVersioning().getVersions());
      givenMetadata.getVersioning().getVersions().forEach(v -> {
        if (!versions.contains(v)) {
          versions.add(v);
        }
      });

      setReleaseAndVersions(newMetadata.getVersioning(),
                            versions);
    }

    final Path tempMetadataFilePath = getMetadataFilePath(registry, groupId, artifactId, true);
    storeMetadata(registry,
                  groupId,
                  artifactId,
                  tempMetadataFilePath,
                  newMetadata);

    final Path metadataFilePath = getMetadataFilePath(registry, groupId, artifactId);
    Files.move(tempMetadataFilePath,
               metadataFilePath,
               StandardCopyOption.ATOMIC_MOVE,
               StandardCopyOption.REPLACE_EXISTING);
  }

  public MetadataCreation initiateMetadataCreation(final Registry registry,
                                                   final String groupId,
                                                   final String artifactId)
    throws IOException {
    final Path metadataPath = getMetadataFilePath(registry, groupId, artifactId);
    Files.createDirectories(metadataPath.getParent());

    final Path tempMetadataPath = getMetadataFilePath(registry, groupId, artifactId, true);
    final OutputStream outputStream = Files.newOutputStream(tempMetadataPath);

    return MetadataCreation.builder()
      .metadataPath(metadataPath)
      .tempMetadataPath(tempMetadataPath)
      .outputStream(outputStream)
      .build();
  }

  public void createMetadata(final Registry registry,
                             final MetadataCreation metadataCreation)
    throws IOException {
    closeSilently(metadataCreation.getOutputStream());

    Files.move(metadataCreation.getTempMetadataPath(),
               metadataCreation.getMetadataPath(),
               StandardCopyOption.ATOMIC_MOVE,
               StandardCopyOption.REPLACE_EXISTING);
  }

  public void abortMetadataCreation(final Registry registry,
                                    final MetadataCreation metadataCreation) {
    closeSilently(metadataCreation.getOutputStream());
    deleteSilently(metadataCreation.getTempMetadataPath());
  }

  public void removeVersionFromMetadata(final Registry registry,
                                        final String groupId,
                                        final String artifactId,
                                        final String version)
    throws Exception {
    final Path metadataFilePath = getMetadataFilePath(registry, groupId, artifactId);
    final MavenMetadata mavenMetadata = loadExistingMetadata(metadataFilePath);
    if (mavenMetadata == null) {
      return;
    }
    final MavenMetadataVersioning versioning = mavenMetadata.getVersioning();
    final List<String> versions = versioning.getVersions();
    versions.remove(version);
    if (versions.isEmpty()) {
      final Path metadataFolderPath = getMetadataFolderPath(registry, groupId, artifactId);
      Files.find(metadataFolderPath,
                 1,
                 (f, basicFileAttributes) -> f.getFileName().toString().startsWith(METADATA_FILE_NAME))
        .forEach(f -> {
          try {
            Files.delete(f);
          } catch (final IOException exception) {
            LOGGER.warn(exception.getMessage(), exception);
          }
        });
    } else {
      if (version.equals(versioning.getRelease())) {
        versioning.setRelease(versions.get(versions.size() - 1));
      }
      // TODO: update last updated?

      storeMetadata(registry,
                    groupId,
                    artifactId,
                    metadataFilePath,
                    mavenMetadata);
    }
  }

  public void setReleaseAndVersions(final MavenMetadataVersioning metadataVersioning,
                                    final List<String> versions) {
    versions.sort(Comparator.comparing(ComparableVersion::new));
    metadataVersioning.setVersions(versions);
    metadataVersioning.setRelease(versions.get(versions.size() - 1));
  }

  private MavenMetadata loadExistingMetadata(final Registry registry,
                                             final String groupId,
                                             final String artifactId)
    throws IOException {
    final Path metadataFilePath = getMetadataFilePath(registry, groupId, artifactId);
    return loadExistingMetadata(metadataFilePath);
  }

  private MavenMetadata loadExistingMetadata(final Path metadataFilePath)
    throws IOException {
    if (!Files.isRegularFile(metadataFilePath)) {
      return null;
    }
    try (InputStream inputStream = Files.newInputStream(metadataFilePath)) {
      return registreeXmlConfiguration.getXmlMapper().readValue(inputStream, MavenMetadata.class);
    }
  }

  private void storeMetadata(final Registry registry,
                             final String groupId,
                             final String artifactId,
                             final Path metadataFilePath,
                             final MavenMetadata mavenMetadata)
    throws Exception {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    registreeXmlConfiguration.getXmlMapper().writeValue(baos,
                                                        mavenMetadata);

    final byte[] bytes = baos.toByteArray();
    Files.write(metadataFilePath, bytes);

    final MessageDigest md5Digest = MessageDigest.getInstance("MD5");
    final byte[] md5Checksum = md5Digest.digest(bytes);
    checksumStorage.publishMetadataChecksum(registry,
                                            groupId,
                                            artifactId,
                                            METADATA_FILE_NAME + ".md5",
                                            bytesToHex(md5Checksum));

    final MessageDigest sha1Digest = MessageDigest.getInstance("SHA1");
    final byte[] sha1Checksum = sha1Digest.digest(bytes);
    checksumStorage.publishMetadataChecksum(registry,
                                            groupId,
                                            artifactId,
                                            METADATA_FILE_NAME + ".sha1",
                                            bytesToHex(sha1Checksum));
  }

  private Path getMetadataFolderPath(final Registry registry,
                                     final String groupId,
                                     final String artifactId) {
    final String groupPath = groupId.replace('.', '/');
    return Paths.get(storageHelper.getStoragePath(registry),
                     groupPath,
                     artifactId);
  }

  private Path getMetadataFilePath(final Registry registry,
                                   final String groupId,
                                   final String artifactId) {
    return getMetadataFilePath(registry,
                               groupId,
                               artifactId,
                               false);
  }

  private Path getMetadataFilePath(final Registry registry,
                                   final String groupId,
                                   final String artifactId,
                                   final boolean temp) {
    final Path folderPath = getMetadataFolderPath(registry, groupId, artifactId);
    final String actualFileName = METADATA_FILE_NAME + (temp ? "." + RandomHelper.random(6) : "");
    return Paths.get(folderPath.toString(),
                     actualFileName);
  }
}
