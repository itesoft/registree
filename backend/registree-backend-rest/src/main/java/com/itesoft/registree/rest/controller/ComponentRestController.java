package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import java.util.List;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.ComponentController;
import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.CreateComponentArgs;
import com.itesoft.registree.dto.DeleteComponentArgs;
import com.itesoft.registree.dto.UpdateComponentArgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/components")
public class ComponentRestController {
  @Autowired
  private ComponentController componentController;

  @RequestMapping(method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<List<Component>> searchComponents(@RequestParam(value = "filter", required = false) final String filter,
                                                          @RequestParam(value = "sort", required = false) final String sort,
                                                          @RequestParam(value = "page", required = false, defaultValue = "0") final Integer page,
                                                          @RequestParam(value = "page_size", required = false, defaultValue = "20") final Integer pageSize) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final List<Component> components = componentController.searchComponents(createDefaultRequestContext(),
                                                                            responseContext,
                                                                            filter,
                                                                            sort,
                                                                            page,
                                                                            pageSize);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(components);
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<Component> getComponent(@PathVariable("id") final String id) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final Component component = componentController.getComponent(createDefaultRequestContext(),
                                                                 responseContext,
                                                                 id);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(component);
  }

  @RequestMapping(method = RequestMethod.POST,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Component> createComponent(@RequestBody final CreateComponentArgs createComponentArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final Component component = componentController.createComponent(createDefaultRequestContext(),
                                                                    responseContext,
                                                                    createComponentArgs);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(component);
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.PUT,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Component> updateComponent(@PathVariable("id") final String id,
                                                   @RequestBody final UpdateComponentArgs updateComponentArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final Component component = componentController.updateComponent(createDefaultRequestContext(),
                                                                    responseContext,
                                                                    id,
                                                                    updateComponentArgs);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(component);
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.DELETE,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Void> deleteComponent(@PathVariable("id") final String id,
                                              @RequestBody(required = false) final DeleteComponentArgs deleteComponentArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    componentController.deleteComponent(createDefaultRequestContext(),
                                        responseContext,
                                        id,
                                        deleteComponentArgs);
    return ResponseEntity.noContent()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .build();
  }
}
