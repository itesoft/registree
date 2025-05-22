package com.itesoft.registree.raw.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class ReadOnlyRawFileManager extends AbstractFileManager implements RawFileManager {
  @Override
  public ResponseEntity<StreamingResponseBody> publishFile(final RawOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String path)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }
}
