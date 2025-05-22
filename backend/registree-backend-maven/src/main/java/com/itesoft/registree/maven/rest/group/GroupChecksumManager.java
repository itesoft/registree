package com.itesoft.registree.maven.rest.group;

import static com.itesoft.registree.java.DigestHelper.bytesToHex;

import java.io.ByteArrayOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.maven.rest.MavenArtifactManager;
import com.itesoft.registree.maven.rest.MavenChecksumManager;
import com.itesoft.registree.maven.rest.MavenMetadataManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.rest.ReadOnlyMavenChecksumManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupChecksumManager extends ReadOnlyMavenChecksumManager implements MavenChecksumManager {
  @Autowired
  private GroupMetadataManager groupMetadataManager;

  @Autowired
  private MavenGroupRegistryHelper mavenGroupRegistryHelper;

  @Override
  public RegistryType getType() {
    return RegistryType.GROUP;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> metadataChecksumExists(final MavenOperationContext context,
                                                                      final HttpServletRequest request,
                                                                      final String groupId,
                                                                      final String artifactId,
                                                                      final String fileName,
                                                                      final String extension)
    throws Exception {
    return mavenGroupRegistryHelper.findAnswer(context,
                                               (subContext, metadataManager) -> {
                                                 return metadataManager.metadataExists(subContext, request, groupId, artifactId);
                                               },
                                               String.format("Checksum %s for metadata %s:%s cannot be found",
                                                             fileName,
                                                             groupId,
                                                             artifactId),
                                               MavenMetadataManager.class);
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
    return mavenGroupRegistryHelper.findAnswer(context,
                                               (subContext, artifactManager) -> {
                                                 return artifactManager.artifactExists(subContext, request, groupId, artifactId, version, fileName);
                                               },
                                               String.format("Checksum %s for artifact %s:%s:%s cannot be found",
                                                             fileName,
                                                             groupId,
                                                             artifactId,
                                                             version),
                                               MavenArtifactManager.class);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getMetadataChecksum(final MavenOperationContext context,
                                                                   final HttpServletRequest request,
                                                                   final String groupId,
                                                                   final String artifactId,
                                                                   final String fileName,
                                                                   final String extension)
    throws Exception {
    final ResponseEntity<StreamingResponseBody> response =
      groupMetadataManager.getMetadata(context,
                                       request,
                                       groupId,
                                       artifactId);

    if (!HttpStatus.OK.equals(response.getStatusCode())) {
      return response;
    }

    final MessageDigest messageDigest = MessageDigest.getInstance(extension);
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DigestOutputStream digestOutputStream = new DigestOutputStream(baos, messageDigest)) {
      response.getBody().writeTo(baos);
    }

    final byte[] digest = messageDigest.digest();
    final String checksum = bytesToHex(digest);

    final StreamingResponseBody stream = outputStream -> {
      outputStream.write(checksum.getBytes());
    };

    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(stream);
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
    return mavenGroupRegistryHelper.findAnswer(context,
                                               (subContext, artifactManager) -> {
                                                 return artifactManager.getArtifact(subContext, request, groupId, artifactId, version, fileName);
                                               },
                                               String.format("Checksum %s for artifact %s:%s:%s cannot be found",
                                                             fileName,
                                                             groupId,
                                                             artifactId,
                                                             version),
                                               MavenArtifactManager.class);
  }
}
