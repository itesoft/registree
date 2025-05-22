package com.itesoft.registree.api.definition;

import java.util.List;

import com.itesoft.registree.dto.ClearProxyRegistryCacheArgs;
import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.DeleteRegistryArgs;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.UpdateRegistryArgs;

public interface RegistryApi {
  List<Registry> searchRegistries(RequestContext requestContext,
                                  ResponseContext responseContext,
                                  String filter,
                                  String sort,
                                  Integer page,
                                  Integer pageSize);

  Registry getRegistry(RequestContext requestContext,
                       ResponseContext responseContext,
                       String name);

  Registry createRegistry(RequestContext requestContext,
                          ResponseContext responseContext,
                          CreateRegistryArgs createRegistryArgs);

  Registry updateRegistry(RequestContext requestContext,
                          ResponseContext responseContext,
                          String name,
                          UpdateRegistryArgs updateRegistryArgs);

  void deleteRegistry(RequestContext requestContext,
                      ResponseContext responseContext,
                      String name,
                      DeleteRegistryArgs deleteRegistryArgs);

  void clearProxyRegistryCache(RequestContext requestContext,
                               ResponseContext responseContext,
                               String name,
                               ClearProxyRegistryCacheArgs clearProxyRegistryCacheArgs);
}
