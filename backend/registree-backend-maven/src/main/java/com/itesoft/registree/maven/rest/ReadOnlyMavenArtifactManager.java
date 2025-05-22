package com.itesoft.registree.maven.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class ReadOnlyMavenArtifactManager extends AbstractMavenManager implements MavenArtifactManager {
  @Override
  public ResponseEntity<StreamingResponseBody> publishArtifact(final MavenOperationContext context,
                                                               final HttpServletRequest request,
                                                               final String groupId,
                                                               final String artifactId,
                                                               final String version,
                                                               final String fileName)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }
}
