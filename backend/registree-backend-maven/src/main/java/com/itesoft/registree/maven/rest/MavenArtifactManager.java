package com.itesoft.registree.maven.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface MavenArtifactManager extends MavenManager {
  ResponseEntity<StreamingResponseBody> artifactExists(MavenOperationContext context,
                                                       HttpServletRequest request,
                                                       String groupId,
                                                       String artifactId,
                                                       String version,
                                                       String fileName)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getArtifact(MavenOperationContext context,
                                                    HttpServletRequest request,
                                                    String groupId,
                                                    String artifactId,
                                                    String version,
                                                    String fileName)
    throws Exception;

  ResponseEntity<StreamingResponseBody> publishArtifact(MavenOperationContext context,
                                                        HttpServletRequest request,
                                                        String groupId,
                                                        String artifactId,
                                                        String version,
                                                        String fileName)
    throws Exception;
}
