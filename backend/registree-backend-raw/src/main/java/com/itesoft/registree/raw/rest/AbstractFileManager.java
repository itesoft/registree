package com.itesoft.registree.raw.rest;

import java.io.InputStream;
import java.nio.file.Files;

import com.itesoft.registree.raw.dto.RawFile;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class AbstractFileManager implements RawFileManager {
  protected ResponseEntity<StreamingResponseBody> getFileLocal(final RawFile file) {
    final byte[] buffer = new byte[10240];
    final StreamingResponseBody stream = outputStream -> {
      try (InputStream inputStream = Files.newInputStream(file.getPath())) {
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, read);
        }
      }
    };

    return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(stream);
  }
}
