package com.itesoft.registree.oci.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface OciRegistryManifestManager extends OciRegistryManager {
  ResponseEntity<StreamingResponseBody> manifestExists(OciOperationContext context,
                                                       HttpServletRequest request,
                                                       String name,
                                                       String reference)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getManifest(OciOperationContext context,
                                                    HttpServletRequest request,
                                                    String name,
                                                    String reference)
    throws Exception;

  ResponseEntity<StreamingResponseBody> createManifest(OciOperationContext context,
                                                       HttpServletRequest request,
                                                       String name,
                                                       String reference,
                                                       byte[] manifest)
    throws Exception;
}
