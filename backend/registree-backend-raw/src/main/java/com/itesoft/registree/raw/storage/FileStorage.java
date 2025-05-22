package com.itesoft.registree.raw.storage;

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
import com.itesoft.registree.raw.api.RawApiCallback;
import com.itesoft.registree.raw.dto.RawFile;
import com.itesoft.registree.raw.dto.RawFileCreation;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class FileStorage {
  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private RawApiCallback apiCallback;

  public RawFile getFile(final Registry registry,
                         final String name) {
    final Path filePath = getFilePath(registry, name);
    if (!Files.isRegularFile(filePath)) {
      return null;
    }
    return RawFile.builder().path(filePath).build();
  }

  public void publishFile(final Registry registry,
                          final String name,
                          final String contentType,
                          final InputStream inputStream)
    throws IOException {
    final Path filePath = getFilePath(registry, name);
    Files.createDirectories(filePath.getParent());
    final byte[] buffer = new byte[10240];
    try (OutputStream outputStream = Files.newOutputStream(filePath)) {
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, read);
      }
    }

    createOrUpdateComponentAndFile(registry, name, contentType);
  }

  public void prepareFileCreation(final Registry registry,
                                  final String name,
                                  final String contentType) {
    createOrUpdateComponentAndFile(registry, name, contentType);
  }

  public RawFileCreation initiateFileCreation(final Registry registry,
                                              final String name)
    throws IOException {
    final Path filePath = getFilePath(registry, name);
    Files.createDirectories(filePath.getParent());

    final Path tempFilePath = getFilePath(registry, name, true);
    final OutputStream outputStream = Files.newOutputStream(tempFilePath);

    return RawFileCreation.builder()
      .filePath(filePath)
      .tempFilePath(tempFilePath)
      .outputStream(outputStream)
      .build();
  }

  public void createFile(final Registry registry,
                         final RawFileCreation rawFileCreation)
    throws IOException {
    closeSilently(rawFileCreation.getOutputStream());

    Files.move(rawFileCreation.getTempFilePath(),
               rawFileCreation.getFilePath(),
               StandardCopyOption.ATOMIC_MOVE,
               StandardCopyOption.REPLACE_EXISTING);
  }

  public void abortFileCreation(final Registry registry,
                                final RawFileCreation rawFileCreation) {
    closeSilently(rawFileCreation.getOutputStream());
    deleteSilently(rawFileCreation.getTempFilePath());
  }

  public void deleteFile(final Registry registry,
                         final String name)
    throws Exception {
    final Path filePath = getFilePath(registry, name);
    Files.deleteIfExists(filePath);
  }

  public String normalizePath(final String name) {
    if (name.startsWith("/")) {
      return name.substring(1);
    } else {
      return name;
    }
  }

  private void createOrUpdateComponentAndFile(final Registry registry,
                                              final String name,
                                              final String contentType) {
    final String normalizedName = normalizePath(name);
    final String actualContentType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;
    final boolean exists = apiCallback.componentExists(registry,
                                                       normalizedName);
    final String componentId;
    if (exists) {
      componentId =
        apiCallback.updateComponent(registry,
                                    normalizedName);
      apiCallback.updateFile(registry,
                             componentId,
                             normalizedName,
                             actualContentType);
    } else {
      componentId =
        apiCallback.createComponent(registry,
                                    normalizedName);
      apiCallback.createFile(registry,
                             componentId,
                             normalizedName,
                             actualContentType);
    }
  }

  private Path getFilePath(final Registry registry,
                           final String name) {
    return getFilePath(registry, name, false);
  }

  private Path getFilePath(final Registry registry,
                           final String name,
                           final boolean temp) {
    final String actualFileName = name + (temp ? "." + RandomHelper.random(6) : "");
    return Paths.get(storageHelper.getStoragePath(registry),
                     actualFileName);
  }
}
