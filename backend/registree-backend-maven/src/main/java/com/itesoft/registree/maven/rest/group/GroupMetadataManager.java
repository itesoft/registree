package com.itesoft.registree.maven.rest.group;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.configuration.RegistreeXmlConfiguration;
import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.maven.dto.xml.MavenMetadata;
import com.itesoft.registree.maven.dto.xml.MavenMetadataVersioning;
import com.itesoft.registree.maven.rest.MavenMetadataManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.rest.ReadOnlyMavenMetadataManager;
import com.itesoft.registree.maven.rest.error.MavenErrorManager;
import com.itesoft.registree.maven.storage.MetadataStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupMetadataManager extends ReadOnlyMavenMetadataManager implements MavenMetadataManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(GroupMetadataManager.class);
  @Autowired
  private MavenGroupRegistryHelper mavenGroupRegistryHelper;

  @Autowired
  private RegistreeXmlConfiguration registreeXmlConfiguration;

  @Autowired
  private MetadataStorage metadataStorage;

  @Autowired
  private MavenErrorManager errorManager;

  @Override
  public RegistryType getType() {
    return RegistryType.GROUP;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> metadataExists(final MavenOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String groupId,
                                                              final String artifactId)
    throws Exception {
    return mavenGroupRegistryHelper.findAnswer(context,
                                               (subContext, metadataManager) -> {
                                                 return metadataManager.metadataExists(subContext, request, groupId, artifactId);
                                               },
                                               String.format("Metadata for %s:%s cannot be found",
                                                             groupId,
                                                             artifactId),
                                               MavenMetadataManager.class);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getMetadata(final MavenOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String groupId,
                                                           final String artifactId)
    throws Exception {
    String lastUpdated = "0";
    final Set<String> versions = new HashSet<>();

    final GroupRegistry groupRegistry = (GroupRegistry) context.getRegistry();
    for (final String member : groupRegistry.getMemberNames()) {
      final MavenOperationContext subContext = context.createSubContext(member);
      final MavenMetadataManager metadataManager = subContext.getMetadataManager();
      final ResponseEntity<StreamingResponseBody> response = metadataManager.getMetadata(subContext,
                                                                                         request,
                                                                                         groupId,
                                                                                         artifactId);
      if (!HttpStatus.OK.equals(response.getStatusCode())) {
        continue;
      }
      final byte[] bytes;
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        response.getBody().writeTo(baos);
        bytes = baos.toByteArray();
      }

      final MavenMetadata subMetadata = registreeXmlConfiguration.getXmlMapper().readValue(bytes, MavenMetadata.class);
      final MavenMetadataVersioning metadataVersioning = subMetadata.getVersioning();
      if (metadataVersioning == null) {
        LOGGER.error(String.format("Metadata versioning is null for %s:%s on registry %s",
                                   groupId,
                                   artifactId,
                                   member));
      } else { // FIXME: can it be null? when?
        if (lastUpdated.compareTo(metadataVersioning.getLastUpdated()) < 0) {
          lastUpdated = metadataVersioning.getLastUpdated();
        }
        versions.addAll(metadataVersioning.getVersions());
      }
    }

    if (versions.isEmpty()) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           String.format("Metadata for %s:%s not found",
                                                         groupId,
                                                         artifactId));
    }

    final MavenMetadataVersioning metadataVersioning = new MavenMetadataVersioning();
    metadataVersioning.setLastUpdated(lastUpdated);
    metadataStorage.setReleaseAndVersions(metadataVersioning,
                                          new ArrayList<>(versions));

    final MavenMetadata mavenMetadata = new MavenMetadata();
    mavenMetadata.setGroupId(groupId);
    mavenMetadata.setArtifactId(artifactId);
    mavenMetadata.setVersioning(metadataVersioning);

    final StreamingResponseBody stream = outputStream -> {
      registreeXmlConfiguration.getXmlMapper().writeValue(outputStream, mavenMetadata);
    };

    return ResponseEntity.ok()
      .contentType(MediaType.APPLICATION_XML)
      .body(stream);
  }
}
