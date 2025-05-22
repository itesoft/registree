package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.UserRouteController;
import com.itesoft.registree.dto.CreateRouteArgs;
import com.itesoft.registree.dto.DeleteRouteArgs;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.Route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{userId}/routes")
public class UserRouteRestController {
  @Autowired
  private UserRouteController userRouteController;

  @RequestMapping(value = "/{path}",
                  method = RequestMethod.POST,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Route> createRoute(final HttpServletRequest request,
                                           @PathVariable("userId") final OneOfLongOrString userId,
                                           @PathVariable("path") final String path,
                                           @RequestBody final CreateRouteArgs createRouteArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final Route route = userRouteController.createRoute(createDefaultRequestContext(),
                                                        responseContext,
                                                        userId,
                                                        path,
                                                        createRouteArgs);
    return ResponseEntity.created(null)
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(route);
  }

  @RequestMapping(value = "/{path}",
                  method = RequestMethod.DELETE,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Void> deleteRoute(@PathVariable("userId") final OneOfLongOrString userId,
                                          @PathVariable("path") final String path,
                                          @RequestBody(required = false) final DeleteRouteArgs deleteRouteArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    userRouteController.deleteRoute(createDefaultRequestContext(),
                                    responseContext,
                                    userId,
                                    path,
                                    deleteRouteArgs);
    return ResponseEntity.noContent()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .build();
  }
}
