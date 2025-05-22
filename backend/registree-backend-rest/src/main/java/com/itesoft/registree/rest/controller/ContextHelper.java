package com.itesoft.registree.rest.controller;

import java.util.HashMap;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;

public interface ContextHelper {
  static RequestContext createDefaultRequestContext() {
    return RequestContext.builder().build();
  }

  static ResponseContext createDefaultResponseContext() {
    return ResponseContext.builder().extraProperties(new HashMap<>()).build();
  }
}
