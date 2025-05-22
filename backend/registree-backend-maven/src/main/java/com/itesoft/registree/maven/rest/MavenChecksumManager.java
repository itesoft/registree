package com.itesoft.registree.maven.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface MavenChecksumManager extends MavenManager {
  ResponseEntity<StreamingResponseBody> metadataChecksumExists(MavenOperationContext context,
                                                               HttpServletRequest request,
                                                               String groupId,
                                                               String artifactId,
                                                               String fileName,
                                                               String extension)
    throws Exception;

  ResponseEntity<StreamingResponseBody> artifactChecksumExists(MavenOperationContext context,
                                                               HttpServletRequest request,
                                                               String groupId,
                                                               String artifactId,
                                                               String version,
                                                               String fileName,
                                                               String extension)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getMetadataChecksum(MavenOperationContext context,
                                                            HttpServletRequest request,
                                                            String groupId,
                                                            String artifactId,
                                                            String fileName,
                                                            String extension)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getArtifactChecksum(MavenOperationContext context,
                                                            HttpServletRequest request,
                                                            String groupId,
                                                            String artifactId,
                                                            String version,
                                                            String fileName,
                                                            String extension)
    throws Exception;

  ResponseEntity<StreamingResponseBody> publishMetadataChecksum(MavenOperationContext context,
                                                                HttpServletRequest request,
                                                                String groupId,
                                                                String artifactId,
                                                                String fileName,
                                                                String extension)
    throws Exception;

  ResponseEntity<StreamingResponseBody> publishArtifactChecksum(MavenOperationContext context,
                                                                HttpServletRequest request,
                                                                String groupId,
                                                                String artifactId,
                                                                String version,
                                                                String fileName,
                                                                String extension)
    throws Exception;
}
