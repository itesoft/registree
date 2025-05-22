package com.itesoft.registree.rest.test;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.DeleteComponentArgs;

@Path("")
public interface ComponentClient {
  @GET
  @Path("/components/{id}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  Component getComponent(@PathParam("id") String id);

  @GET
  @Path("/registries/{registryName}/components/{gav}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  Component getComponent(@PathParam("registryName") String registryName,
                         @PathParam("gav") @Encoded String gav);

  @DELETE
  @Path("/components/{id}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  void deleteComponent(@PathParam("id") String id,
                       DeleteComponentArgs deleteComponentArgs);

  @DELETE
  @Path("/registries/{registryName}/components/{gav}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  void deleteComponent(@PathParam("registryName") String registryName,
                       @PathParam("gav") String gav,
                       DeleteComponentArgs deleteComponentArgs);
}
