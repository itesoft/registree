package com.itesoft.registree.oci.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class ReadOnlyRegistryBlobUploadManager implements OciRegistryBlobUploadManager {
  @Override
  public ResponseEntity<StreamingResponseBody> mountBlob(final OciOperationContext context,
                                                         final HttpServletRequest request,
                                                         final String name,
                                                         final String from,
                                                         final String mount)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getUploadRange(final OciOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String name,
                                                              final String uuid)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }

  @Override
  public ResponseEntity<StreamingResponseBody> startUpload(final OciOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String name,
                                                           final String digest)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }

  @Override
  public ResponseEntity<StreamingResponseBody> doUpload(final OciOperationContext context,
                                                        final HttpServletRequest request,
                                                        final String name,
                                                        final String uuid,
                                                        final String digest)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }

  @Override
  public ResponseEntity<StreamingResponseBody> doUploadChunk(final OciOperationContext context,
                                                             final HttpServletRequest request,
                                                             final String name,
                                                             final String uuid)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }
}
