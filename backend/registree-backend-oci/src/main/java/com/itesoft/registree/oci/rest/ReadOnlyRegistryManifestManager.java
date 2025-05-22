package com.itesoft.registree.oci.rest;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class ReadOnlyRegistryManifestManager extends AbstractRegistryManifestManager implements OciRegistryManifestManager {
  @Override
  public ResponseEntity<StreamingResponseBody> createManifest(final OciOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String name,
                                                              final String reference,
                                                              final byte[] manifest)
    throws IOException {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .build();
  }
}
