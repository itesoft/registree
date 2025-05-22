package com.itesoft.registree.maven.rest.hosted;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.maven.rest.AbstractMavenManager;
import com.itesoft.registree.maven.rest.MavenArtifactManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.storage.ArtifactStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedArtifactManager extends AbstractMavenManager implements MavenArtifactManager {
  @Autowired
  private ArtifactStorage artifactStorage;

  @Override
  public RegistryType getType() {
    return RegistryType.HOSTED;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> artifactExists(final MavenOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String groupId,
                                                              final String artifactId,
                                                              final String version,
                                                              final String fileName)
    throws Exception {
    final MavenFile mavenFile =
      artifactStorage.getArtifactFile(context.getRegistry(),
                                      groupId,
                                      artifactId,
                                      version,
                                      fileName);
    return localFileExists(mavenFile,
                           () -> String.format("Artifact %s:%s:%s not found",
                                               groupId,
                                               artifactId,
                                               version));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getArtifact(final MavenOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String groupId,
                                                           final String artifactId,
                                                           final String version,
                                                           final String fileName)
    throws Exception {
    final MavenFile mavenFile =
      artifactStorage.getArtifactFile(context.getRegistry(),
                                      groupId,
                                      artifactId,
                                      version,
                                      fileName);
    return getLocalFile(mavenFile,
                        () -> String.format("Artifact %s:%s:%s not found",
                                            groupId,
                                            artifactId,
                                            version));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> publishArtifact(final MavenOperationContext context,
                                                               final HttpServletRequest request,
                                                               final String groupId,
                                                               final String artifactId,
                                                               final String version,
                                                               final String fileName)
    throws Exception {
    artifactStorage.publishArtifactFile(context.getRegistry(),
                                        groupId,
                                        artifactId,
                                        version,
                                        fileName,
                                        request.getInputStream());
    return ResponseEntity.created(null).build();
  }
}
