package com.itesoft.registree.oci.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface OciRegistryRepositoryManager extends OciRegistryManager {
  ResponseEntity<StreamingResponseBody> getCatalog(OciOperationContext context,
                                                   HttpServletRequest request)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getTags(OciOperationContext context,
                                                HttpServletRequest request,
                                                String name)
    throws Exception;
}
