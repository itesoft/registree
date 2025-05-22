package com.itesoft.registree.oci.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface OciRegistryBlobManager extends OciRegistryManager {
  ResponseEntity<StreamingResponseBody> blobExists(OciOperationContext context,
                                                   HttpServletRequest request,
                                                   String name,
                                                   String digest)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getBlob(OciOperationContext context,
                                                HttpServletRequest request,
                                                String digest)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getBlob(OciOperationContext context,
                                                HttpServletRequest request,
                                                String name,
                                                String digest)
    throws Exception;
}
