package com.itesoft.registree.npm.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class ReadOnlyNpmPackageManager extends AbstractPackageManager implements NpmPackageManager {
  @Override
  public ResponseEntity<StreamingResponseBody> publishPackage(final NpmOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String packageScope,
                                                              final String packageName)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }
}
