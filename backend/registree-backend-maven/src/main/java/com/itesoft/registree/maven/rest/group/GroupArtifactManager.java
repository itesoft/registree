package com.itesoft.registree.maven.rest.group;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.maven.rest.MavenArtifactManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.rest.ReadOnlyMavenArtifactManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupArtifactManager extends ReadOnlyMavenArtifactManager implements MavenArtifactManager {
  @Autowired
  private MavenGroupRegistryHelper mavenGroupRegistryHelper;

  @Override
  public RegistryType getType() {
    return RegistryType.GROUP;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> artifactExists(final MavenOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String groupId,
                                                              final String artifactId,
                                                              final String version,
                                                              final String fileName)
    throws Exception {
    return mavenGroupRegistryHelper.findAnswer(context,
                                               (subContext, artifactManager) -> {
                                                 return artifactManager.artifactExists(subContext, request, groupId, artifactId, version, fileName);
                                               },
                                               String.format("Artifact %s:%s:%s cannot be found",
                                                             groupId,
                                                             artifactId,
                                                             version),
                                               MavenArtifactManager.class);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getArtifact(final MavenOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String groupId,
                                                           final String artifactId,
                                                           final String version,
                                                           final String fileName)
    throws Exception {
    return mavenGroupRegistryHelper.findAnswer(context,
                                               (subContext, artifactManager) -> {
                                                 return artifactManager.getArtifact(subContext, request, groupId, artifactId, version, fileName);
                                               },
                                               String.format("Artifact %s:%s:%s cannot be found",
                                                             groupId,
                                                             artifactId,
                                                             version),
                                               MavenArtifactManager.class);
  }
}
