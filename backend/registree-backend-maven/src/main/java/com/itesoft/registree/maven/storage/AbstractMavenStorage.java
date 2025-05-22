package com.itesoft.registree.maven.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

abstract class AbstractMavenStorage {
  private static final Map<String, String> EXTENSION_TO_CONTENT_TYPES = new HashMap<>();

  static {
    EXTENSION_TO_CONTENT_TYPES.put("xml", MediaType.APPLICATION_XML_VALUE);
    EXTENSION_TO_CONTENT_TYPES.put("pom", MediaType.APPLICATION_XML_VALUE);
  }

  @Autowired
  private StorageHelper storageHelper;

  protected void storeFile(final Path filePath,
                           final Path tempFilePath,
                           final InputStream inputStream)
    throws IOException {
    storeFile(tempFilePath, inputStream);

    Files.move(tempFilePath,
               filePath,
               StandardCopyOption.ATOMIC_MOVE,
               StandardCopyOption.REPLACE_EXISTING);
  }

  protected void storeFile(final Path filePath,
                           final InputStream inputStream)
    throws IOException {
    Files.createDirectories(filePath.getParent());
    final byte[] buffer = new byte[10240];
    try (OutputStream outputStream = Files.newOutputStream(filePath)) {
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, read);
      }
    }
  }

  protected String getContentType(final String fileName)
    throws IOException {
    final int index = fileName.lastIndexOf('.');
    final String extension = fileName.substring(index + 1);
    final String contentType = EXTENSION_TO_CONTENT_TYPES.get(extension);
    if (contentType == null) {
      return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
    return contentType;
  }

  protected Path relativize(final Registry registry,
                            final Path path) {
    final Path rootPath = Paths.get(storageHelper.getStoragePath(registry));
    return rootPath.relativize(path);
  }
}
