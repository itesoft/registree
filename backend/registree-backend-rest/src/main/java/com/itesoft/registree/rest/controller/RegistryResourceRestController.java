package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import java.util.List;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.RegistryResourceController;
import com.itesoft.registree.dto.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registries/{registryName}/resources")
public class RegistryResourceRestController {
  @Autowired
  private RegistryResourceController registryResourceController;

  @RequestMapping(value = { "", "/" },
                  method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<List<Resource>> getRootResources(@PathVariable("registryName") final String registryName) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final List<Resource> resources =
      registryResourceController.getRootResources(createDefaultRequestContext(),
                                                  responseContext,
                                                  registryName);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(resources);
  }

  @RequestMapping(value = "/{path}",
                  method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<List<Resource>> getResources(@PathVariable("registryName") final String registryName,
                                                     @PathVariable("path") final String path) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final List<Resource> resources =
      registryResourceController.getResources(createDefaultRequestContext(),
                                              responseContext,
                                              registryName,
                                              path);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(resources);
  }
}
