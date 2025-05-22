package com.itesoft.registree.oci.rest.hosted;

import static com.itesoft.registree.java.DigestHelper.bytesToHex;
import static com.itesoft.registree.oci.rest.error.ErrorCode.MANIFEST_INVALID;
import static com.itesoft.registree.oci.rest.error.ErrorCode.MANIFEST_UNKNOWN;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.dto.Manifest;
import com.itesoft.registree.oci.dto.json.ManifestDto;
import com.itesoft.registree.oci.rest.AbstractRegistryManifestManager;
import com.itesoft.registree.oci.rest.OciRegistryManifestManager;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.error.OciErrorManager;
import com.itesoft.registree.oci.storage.RepositoryStorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedRegistryManifestManager extends AbstractRegistryManifestManager implements OciRegistryManifestManager {
  @Autowired
  private OciErrorManager errorManager;

  @Autowired
  private RepositoryStorage repositoryStorage;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public RegistryType getType() {
    return RegistryType.HOSTED;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> manifestExists(final OciOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String name,
                                                              final String reference)
    throws IOException {
    return getManifest(context, request, name, reference, false);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getManifest(final OciOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String name,
                                                           final String reference)
    throws IOException {
    return getManifest(context, request, name, reference, true);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> createManifest(final OciOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String name,
                                                              final String reference,
                                                              final byte[] manifest)
    throws NoSuchAlgorithmException, IOException {
    final ManifestDto manifestDto = objectMapper.readValue(manifest, ManifestDto.class);
    final int schemaVersion = manifestDto.getSchemaVersion();
    if (schemaVersion != 2) {
      return errorManager.getErrorResponse(HttpStatus.BAD_REQUEST,
                                           MANIFEST_INVALID,
                                           "Manifest schema version should be 2");
    }

    final String contentType = request.getContentType();

    final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    final byte[] hex = messageDigest.digest(manifest);
    final String digest = String.format("sha256:%s", bytesToHex(hex));

    repositoryStorage.createManifest(context.getRegistry(), name, reference, digest, contentType, manifest);

    final String location = String.format("/v2/%s/manifests/%s", name, reference);
    final HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_LENGTH, "0");
    headers.add(HttpHeaders.LOCATION, location);
    headers.add("Docker-Content-Digest", digest);

    return ResponseEntity.created(URI.create(location))
      .headers(headers)
      .build();
  }

  private ResponseEntity<StreamingResponseBody> getManifest(final OciOperationContext context,
                                                            final HttpServletRequest request,
                                                            final String name,
                                                            final String reference,
                                                            final boolean withData)
    throws IOException {
    final Manifest manifest = getManifest(context, name, reference, withData);
    if (manifest == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           MANIFEST_UNKNOWN,
                                           String.format("Cannot find manifest with ref %s on repository %s", reference, name));
    }

    return getManifestResponse(manifest, withData);
  }
}
