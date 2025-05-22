package com.itesoft.registree.rest.test;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Encoded;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import com.itesoft.registree.dto.DeleteFileArgs;
import com.itesoft.registree.dto.File;

@Path("")
public interface FileClient {
  @GET
  @Path("/files")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  List<File> searchFiles(@QueryParam("filter") String filter,
                         @QueryParam("sort") String sort,
                         @QueryParam("page") String page,
                         @QueryParam("page_size") String pageSize);

  @GET
  @Path("/files/{id}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  File getFile(@PathParam("id") String id);

  @GET
  @Path("/registries/{registryName}/files/{path}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  File getFile(@PathParam("registryName") String registryName,
               @PathParam("path") @Encoded String path);

  @DELETE
  @Path("/files/{id}")
  @Consumes({ "application/json" })
  @Produces({ "application/json" })
  void deleteFile(@PathParam("id") String id,
                  DeleteFileArgs deleteFileArgs);
}
