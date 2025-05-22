package com.itesoft.registree.api.definition;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.itesoft.registree.dto.CreateFileArgs;
import com.itesoft.registree.dto.DeleteFileArgs;
import com.itesoft.registree.dto.File;
import com.itesoft.registree.dto.UpdateFileArgs;

public interface FileApi {
  List<File> searchFiles(RequestContext requestContext,
                         ResponseContext responseContext,
                         String filter,
                         String sort,
                         Integer page,
                         Integer pageSize);

  boolean fileExists(RequestContext requestContext,
                     ResponseContext responseContext,
                     @NotNull String id);

  boolean fileExists(RequestContext requestContext,
                     ResponseContext responseContext,
                     @NotNull String registryName,
                     @NotNull String path);

  File getFile(RequestContext requestContext,
               ResponseContext responseContext,
               @NotNull String id);

  File getFile(RequestContext requestContext,
               ResponseContext responseContext,
               @NotNull String registryName,
               @NotNull String path);

  File createFile(RequestContext requestContext,
                  ResponseContext responseContext,
                  @NotNull CreateFileArgs createFileArgs);

  File updateFile(RequestContext requestContext,
                  ResponseContext responseContext,
                  @NotNull String id,
                  @NotNull UpdateFileArgs updateFileArgs);

  File updateFile(RequestContext requestContext,
                  ResponseContext responseContext,
                  @NotNull String registryName,
                  @NotNull String path,
                  @NotNull UpdateFileArgs updateFileArgs);

  void deleteFile(RequestContext requestContext,
                  ResponseContext responseContext,
                  @NotNull String id,
                  DeleteFileArgs deleteFileArgs);
}
