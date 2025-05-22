package com.itesoft.registree.npm.rest;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface NpmPackageManager extends NpmManager {
  ResponseEntity<StreamingResponseBody> getPackage(NpmOperationContext context,
                                                   HttpServletRequest request,
                                                   String packageScope,
                                                   String packageName)
    throws Exception;

  ResponseEntity<StreamingResponseBody> getPackageVersion(NpmOperationContext context,
                                                          HttpServletRequest request,
                                                          String packageScope,
                                                          String packageName,
                                                          String packageVersion)
    throws Exception;

  ResponseEntity<StreamingResponseBody> publishPackage(NpmOperationContext context,
                                                       HttpServletRequest request,
                                                       String packageScope,
                                                       String packageName)
    throws Exception;

  ResponseEntity<StreamingResponseBody> downloadTarball(NpmOperationContext context,
                                                        HttpServletRequest request,
                                                        String packageScope,
                                                        String packageName,
                                                        String fileName)
    throws Exception;
}
