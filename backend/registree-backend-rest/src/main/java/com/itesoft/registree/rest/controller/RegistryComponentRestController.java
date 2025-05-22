package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.ComponentController;
import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.DeleteComponentArgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registries/{registryName}/components")
public class RegistryComponentRestController {
  @Autowired
  private ComponentController componentController;

  @RequestMapping(value = "/{gav}",
                  method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<Component> getComponent(@PathVariable("registryName") final String registryName,
                                                @PathVariable("gav") final String gav) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final Component component = componentController.getComponent(createDefaultRequestContext(),
                                                                 responseContext,
                                                                 registryName,
                                                                 gav);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(component);
  }

  @RequestMapping(value = "/{gav}",
                  method = RequestMethod.DELETE,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Void> deleteComponent(@PathVariable("registryName") final String registryName,
                                              @PathVariable("gav") final String gav,
                                              @RequestBody(required = false) final DeleteComponentArgs deleteComponentArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    componentController.deleteComponent(createDefaultRequestContext(),
                                        responseContext,
                                        registryName,
                                        gav,
                                        deleteComponentArgs);
    return ResponseEntity.noContent()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .build();
  }
}
