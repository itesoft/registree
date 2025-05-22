package com.itesoft.registree.maven.rest.hosted;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.maven.rest.AbstractMavenManager;
import com.itesoft.registree.maven.rest.MavenMetadataManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.storage.MetadataStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedMetadataManager extends AbstractMavenManager implements MavenMetadataManager {
  @Autowired
  private MetadataStorage metadataStorage;

  @Override
  public RegistryType getType() {
    return RegistryType.HOSTED;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> metadataExists(final MavenOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String groupId,
                                                              final String artifactId)
    throws Exception {
    final MavenFile mavenFile =
      metadataStorage.getMetadataFile(context.getRegistry(),
                                      groupId,
                                      artifactId);
    return localFileExists(mavenFile,
                           () -> String.format("Metadata for %s:%s not found",
                                               groupId,
                                               artifactId));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getMetadata(final MavenOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String groupId,
                                                           final String artifactId)
    throws Exception {
    final MavenFile mavenFile =
      metadataStorage.getMetadataFile(context.getRegistry(),
                                      groupId,
                                      artifactId);
    return getLocalFile(mavenFile,
                        () -> String.format("Metadata for %s:%s not found",
                                            groupId,
                                            artifactId));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> publishMetadata(final MavenOperationContext context,
                                                               final HttpServletRequest request,
                                                               final String groupId,
                                                               final String artifactId)
    throws Exception {
    metadataStorage.publishMetadataFile(context.getRegistry(),
                                        groupId,
                                        artifactId,
                                        request.getInputStream());
    return ResponseEntity.created(null).build();
  }
}
