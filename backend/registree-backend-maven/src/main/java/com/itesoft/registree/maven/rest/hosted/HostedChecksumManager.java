package com.itesoft.registree.maven.rest.hosted;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.maven.rest.AbstractMavenManager;
import com.itesoft.registree.maven.rest.MavenChecksumManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.storage.ChecksumStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedChecksumManager extends AbstractMavenManager implements MavenChecksumManager {
  @Autowired
  private ChecksumStorage checksumStorage;

  @Override
  public RegistryType getType() {
    return RegistryType.HOSTED;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> metadataChecksumExists(final MavenOperationContext context,
                                                                      final HttpServletRequest request,
                                                                      final String groupId,
                                                                      final String artifactId,
                                                                      final String fileName,
                                                                      final String extension)
    throws Exception {
    final MavenFile mavenFile =
      checksumStorage.getMetadataChecksumFile(context.getRegistry(),
                                              groupId,
                                              artifactId,
                                              fileName);
    return localFileExists(mavenFile,
                           () -> String.format("Checksum %s for metadata %s:%s not found",
                                               fileName,
                                               groupId,
                                               artifactId));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> artifactChecksumExists(final MavenOperationContext context,
                                                                      final HttpServletRequest request,
                                                                      final String groupId,
                                                                      final String artifactId,
                                                                      final String version,
                                                                      final String fileName,
                                                                      final String extension)
    throws Exception {
    final MavenFile mavenFile =
      checksumStorage.getArtifactChecksumFile(context.getRegistry(),
                                              groupId,
                                              artifactId,
                                              version,
                                              fileName);
    return localFileExists(mavenFile,
                           () -> String.format("Checksum %s for artifact %s:%s:%s not found",
                                               fileName,
                                               groupId,
                                               artifactId,
                                               version));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getMetadataChecksum(final MavenOperationContext context,
                                                                   final HttpServletRequest request,
                                                                   final String groupId,
                                                                   final String artifactId,
                                                                   final String fileName,
                                                                   final String extension)
    throws Exception {
    final MavenFile mavenFile =
      checksumStorage.getMetadataChecksumFile(context.getRegistry(),
                                              groupId,
                                              artifactId,
                                              fileName);
    return getLocalFile(mavenFile,
                        () -> String.format("Checksum %s for metadata %s:%s not found",
                                            fileName,
                                            groupId,
                                            artifactId));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getArtifactChecksum(final MavenOperationContext context,
                                                                   final HttpServletRequest request,
                                                                   final String groupId,
                                                                   final String artifactId,
                                                                   final String version,
                                                                   final String fileName,
                                                                   final String extension)
    throws Exception {
    final MavenFile mavenFile =
      checksumStorage.getArtifactChecksumFile(context.getRegistry(),
                                              groupId,
                                              artifactId,
                                              version,
                                              fileName);
    return getLocalFile(mavenFile,
                        () -> String.format("Checksum %s for artifact %s:%s:%s not found",
                                            fileName,
                                            groupId,
                                            artifactId,
                                            version));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> publishMetadataChecksum(final MavenOperationContext context,
                                                                       final HttpServletRequest request,
                                                                       final String groupId,
                                                                       final String artifactId,
                                                                       final String fileName,
                                                                       final String extension)
    throws Exception {
    final MavenFile checksumFile =
      checksumStorage.getMetadataChecksumFile(context.getRegistry(),
                                              groupId,
                                              artifactId,
                                              fileName);
    // we store only if not existing, if it exists, we do nothing, since we calculate our own checksums
    if (checksumFile == null) {
      checksumStorage.publishMetadataChecksum(context.getRegistry(),
                                              groupId,
                                              artifactId,
                                              fileName,
                                              request.getInputStream());
    }
    return ResponseEntity.created(null).build();
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
    checksumStorage.publishArtifactChecksum(context.getRegistry(),
                                            groupId,
                                            artifactId,
                                            version,
                                            fileName,
                                            request.getInputStream());
    return ResponseEntity.created(null).build();
  }
}
