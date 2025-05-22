package com.itesoft.registree.maven.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface MavenMetadataManager extends MavenManager {
  ResponseEntity<StreamingResponseBody> metadataExists(MavenOperationContext context,
                                                       HttpServletRequest request,
                                                       String groupId,
                                                       String artifactId)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getMetadata(MavenOperationContext context,
                                                    HttpServletRequest request,
                                                    String groupId,
                                                    String artifactId)
    throws Exception;

  ResponseEntity<StreamingResponseBody> publishMetadata(MavenOperationContext context,
                                                        HttpServletRequest request,
                                                        String groupId,
                                                        String artifactId)
    throws Exception;
}
