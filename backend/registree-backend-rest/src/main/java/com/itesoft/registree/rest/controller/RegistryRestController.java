package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.RegistryController;
import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.DeleteRegistryArgs;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.UpdateRegistryArgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registries")
public class RegistryRestController {
  @Autowired
  private RegistryController registryController;

  @RequestMapping(method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<List<Registry>> searchRegistries(@RequestParam(value = "filter", required = false) final String filter,
                                                         @RequestParam(value = "sort", required = false) final String sort,
                                                         @RequestParam(value = "page", required = false, defaultValue = "0") final Integer page,
                                                         @RequestParam(value = "page_size", required = false, defaultValue = "20") final Integer pageSize) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final List<Registry> registries = registryController.searchRegistries(createDefaultRequestContext(),
                                                                          responseContext,
                                                                          filter,
                                                                          sort,
                                                                          page,
                                                                          pageSize);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(registries);
  }

  @RequestMapping(value = "/{name}",
                  method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<Registry> getRegistry(@PathVariable("name") final String name) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final Registry registry = registryController.getRegistry(createDefaultRequestContext(),
                                                             responseContext,
                                                             name);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(registry);
  }

  @RequestMapping(method = RequestMethod.POST,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Registry> createRegistry(final HttpServletRequest request,
                                                 @RequestBody final CreateRegistryArgs createRegistryArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final Registry registry = registryController.createRegistry(createDefaultRequestContext(),
                                                                responseContext,
                                                                createRegistryArgs);
    return ResponseEntity.created(null)
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(registry);
  }

  @RequestMapping(value = "/{name}",
                  method = RequestMethod.PUT,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Registry> updateRegistry(@PathVariable("name") final String name,
                                                 @RequestBody final UpdateRegistryArgs updateRegistryArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final Registry registry = registryController.updateRegistry(createDefaultRequestContext(),
                                                                responseContext,
                                                                name,
                                                                updateRegistryArgs);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(registry);
  }

  @RequestMapping(value = "/{name}",
                  method = RequestMethod.DELETE,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Void> deleteRegistry(@PathVariable("name") final String name,
                                             @RequestBody(required = false) final DeleteRegistryArgs deleteRegistryArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    registryController.deleteRegistry(createDefaultRequestContext(),
                                      responseContext,
                                      name,
                                      deleteRegistryArgs);
    return ResponseEntity.noContent()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .build();
  }
}
