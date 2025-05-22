package com.itesoft.registree.oci.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface OciRegistryBlobUploadManager extends OciRegistryManager {
  ResponseEntity<StreamingResponseBody> mountBlob(OciOperationContext context,
                                                  HttpServletRequest request,
                                                  String name,
                                                  String from,
                                                  String mount)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getUploadRange(OciOperationContext context,
                                                       HttpServletRequest request,
                                                       String name,
                                                       String uuid)
    throws Exception;

  ResponseEntity<StreamingResponseBody> startUpload(OciOperationContext context,
                                                    HttpServletRequest request,
                                                    String name,
                                                    String digest)
    throws Exception;

  ResponseEntity<StreamingResponseBody> doUpload(OciOperationContext context,
                                                 HttpServletRequest request,
                                                 String name,
                                                 String uuid,
                                                 String digest)
    throws Exception;

  ResponseEntity<StreamingResponseBody> doUploadChunk(OciOperationContext context,
                                                      HttpServletRequest request,
                                                      String name,
                                                      String uuid)
    throws Exception;
}
