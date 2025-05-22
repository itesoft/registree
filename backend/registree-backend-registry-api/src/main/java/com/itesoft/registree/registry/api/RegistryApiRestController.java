package com.itesoft.registree.registry.api;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.Registry;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface RegistryApiRestController {
  String getFormat();

  ResponseEntity<StreamingResponseBody> api(Registry registry,
                                            HttpServletRequest request)
    throws Exception;
}
