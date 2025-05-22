package com.itesoft.registree.rest.test;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import com.itesoft.registree.dto.CreateUserArgs;
import com.itesoft.registree.dto.User;

@Path("")
public interface UserClient {
  @POST
  @Path("/users")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  User createUser(CreateUserArgs createUserArgs);
}
