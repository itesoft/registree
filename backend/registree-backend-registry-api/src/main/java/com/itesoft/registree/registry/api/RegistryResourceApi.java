package com.itesoft.registree.registry.api;

import java.util.List;

import com.itesoft.registree.dto.Resource;

public interface RegistryResourceApi {
  String DIRECTORY_TYPE = "directory";
  String FILE_TYPE = "file";

  String getFormat();

  List<Resource> getRootResources(String registryName);

  List<Resource> getResources(String registryName, String path);
}
