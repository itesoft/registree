package com.itesoft.registree.raw.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface RawFileManager extends RawManager {
  ResponseEntity<StreamingResponseBody> getFile(RawOperationContext context,
                                                HttpServletRequest request,
                                                String path)
    throws Exception;

  ResponseEntity<StreamingResponseBody> publishFile(RawOperationContext context,
                                                    HttpServletRequest request,
                                                    String path)
    throws Exception;
}
