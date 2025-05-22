package com.itesoft.registree.rest.test;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

import com.itesoft.registree.dto.Resource;

@Path("")
public interface RegistryResourceClient {
  @GET
  @Path("/registries/{registryName}/resources")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  List<Resource> getRootResources(@PathParam("registryName") String registryName);

  @GET
  @Path("/registries/{registryName}/resources/{path}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  List<Resource> getResources(@PathParam("registryName") String registryName,
                              @PathParam("path") @Encoded String path);
}
