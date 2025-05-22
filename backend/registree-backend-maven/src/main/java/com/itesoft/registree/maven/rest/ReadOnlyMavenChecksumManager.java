package com.itesoft.registree.maven.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class ReadOnlyMavenChecksumManager extends AbstractMavenManager implements MavenChecksumManager {
  @Override
  public ResponseEntity<StreamingResponseBody> publishMetadataChecksum(final MavenOperationContext context,
                                                                       final HttpServletRequest request,
                                                                       final String groupId,
                                                                       final String artifactId,
                                                                       final String fileName,
                                                                       final String extension)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }

  @Override
  public ResponseEntity<StreamingResponseBody> publishArtifactChecksum(final MavenOperationContext context,
                                                                       final HttpServletRequest request,
                                                                       final String groupId,
                                                                       final String artifactId,
                                                                       final String version,
                                                                       final String fileName,
                                                                       final String extension)
    throws Exception {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }
}
