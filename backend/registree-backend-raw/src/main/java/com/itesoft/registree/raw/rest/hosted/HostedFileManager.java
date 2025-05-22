package com.itesoft.registree.raw.rest.hosted;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.raw.dto.RawFile;
import com.itesoft.registree.raw.rest.AbstractFileManager;
import com.itesoft.registree.raw.rest.RawFileManager;
import com.itesoft.registree.raw.rest.RawOperationContext;
import com.itesoft.registree.raw.storage.FileStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedFileManager extends AbstractFileManager implements RawFileManager {
  @Autowired
  private FileStorage fileStorage;

  @Override
  public RegistryType getType() {
    return RegistryType.HOSTED;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getFile(final RawOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String path) {
    final RawFile file = fileStorage.getFile(context.getRegistry(), path);
    if (file == null) {
      return ResponseEntity.notFound().build();
    }

    return getFileLocal(file);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> publishFile(final RawOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String path)
    throws Exception {
    final String contentType = request.getContentType();
    fileStorage.publishFile(context.getRegistry(),
                            path,
                            contentType,
                            request.getInputStream());
    return ResponseEntity.created(null).build();
  }
}
