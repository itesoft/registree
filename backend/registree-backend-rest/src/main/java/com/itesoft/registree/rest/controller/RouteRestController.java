package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import java.util.List;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.RouteController;
import com.itesoft.registree.dto.Route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteRestController {
  @Autowired
  private RouteController routeController;

  @RequestMapping(method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<List<Route>> searchRoutes(@RequestParam(value = "filter", required = false) final String filter,
                                                  @RequestParam(value = "sort", required = false) final String sort,
                                                  @RequestParam(value = "page", required = false, defaultValue = "0") final Integer page,
                                                  @RequestParam(value = "page_size", required = false, defaultValue = "20") final Integer pageSize) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final List<Route> routes = routeController.searchRoutes(createDefaultRequestContext(),
                                                            responseContext,
                                                            filter,
                                                            sort,
                                                            page,
                                                            pageSize);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(routes);
  }
}
