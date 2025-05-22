package com.itesoft.registree.rest.test;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import com.itesoft.registree.dto.CreateRouteArgs;
import com.itesoft.registree.dto.DeleteRouteArgs;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.Route;

@Path("")
public interface UserRouteClient {
  @POST
  @Path("/users/{userId}/routes/{path}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  Route createRoute(@PathParam("userId") OneOfLongOrString userId,
                    @PathParam("path") @Encoded String path,
                    CreateRouteArgs createRouteArgs);

  @DELETE
  @Path("/users/{userId}/routes/{path}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  void deleteRoute(@PathParam("userId") OneOfLongOrString userId,
                   @PathParam("path") @Encoded String path,
                   DeleteRouteArgs deleteDocumentArgs);
}
