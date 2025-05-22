package com.itesoft.registree.maven.rest;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Supplier;

import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.maven.rest.error.MavenErrorManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class AbstractMavenManager {
  @Autowired
  private MavenErrorManager errorManager;

  protected ResponseEntity<StreamingResponseBody> localFileExists(final MavenFile file,
                                                                  final Supplier<String> errorMessageSupplier) {
    if (file == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND, errorMessageSupplier.get());
    }
    return ResponseEntity.ok().build();
  }

  protected ResponseEntity<StreamingResponseBody> getLocalFile(final MavenFile file,
                                                               final Supplier<String> errorMessageSupplier) {
    if (file == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND, errorMessageSupplier.get());
    }

    final byte[] buffer = new byte[10240];
    final StreamingResponseBody stream = outputStream -> {
      try (InputStream inputStream = Files.newInputStream(file.getPath())) {
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, read);
        }
      }
    };

    return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.getContentType())).body(stream);
  }
}
