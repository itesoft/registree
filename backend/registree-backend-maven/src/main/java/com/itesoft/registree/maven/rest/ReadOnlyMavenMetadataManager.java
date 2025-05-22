package com.itesoft.registree.maven.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class ReadOnlyMavenMetadataManager extends AbstractMavenManager implements MavenMetadataManager {
  @Override
  public ResponseEntity<StreamingResponseBody> publishMetadata(final MavenOperationContext context,
                                                               final HttpServletRequest request,
                                                               final String groupId,
                                                               final String artifactId)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }
}
