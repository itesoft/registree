package com.itesoft.registree.rest.test;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.Registry;

@Path("")
public interface RegistryClient {
  @POST
  @Path("/registries")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  Registry createRegistry(CreateRegistryArgs createRegistryArgs);
}
