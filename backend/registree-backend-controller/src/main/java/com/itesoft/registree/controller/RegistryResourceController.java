package com.itesoft.registree.controller;

import static com.itesoft.registree.api.definition.WellKnownProperties.TOTAL_COUNT;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.Resource;
import com.itesoft.registree.exception.BadRequestException;
import com.itesoft.registree.registry.RegistriesStore;
import com.itesoft.registree.registry.api.RegistryResourceApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegistryResourceController {
  @Autowired
  private RegistriesStore registriesStore;

  private final Map<String, RegistryResourceApi> registryResourceApis = new ConcurrentHashMap<>();;

  @Autowired(required = false)
  private void setupRegistryResourceApis(final List<RegistryResourceApi> registryResourceApis) {
    for (final RegistryResourceApi registryResourceApi : registryResourceApis) {
      this.registryResourceApis.put(registryResourceApi.getFormat(), registryResourceApi);
    }
  }

  public List<Resource> getRootResources(final RequestContext requestContext,
                                         final ResponseContext responseContext,
                                         final String registryName) {
    return getResources(responseContext,
                        registryName,
                        (registryResourceApi) -> registryResourceApi.getRootResources(registryName));
  }

  public List<Resource> getResources(final RequestContext requestContext,
                                     final ResponseContext responseContext,
                                     final String registryName,
                                     final String path) {
    return getResources(responseContext,
                        registryName,
                        (registryResourceApi) -> registryResourceApi.getResources(registryName, path));
  }

  private List<Resource> getResources(final ResponseContext responseContext,
                                      final String registryName,
                                      final Function<RegistryResourceApi, List<Resource>> resourcesApi) {
    final RegistryResourceApi registryResourceApi = getRegistryResourceApi(registryName);
    final List<Resource> resources = resourcesApi.apply(registryResourceApi);
    responseContext.getExtraProperties().put(TOTAL_COUNT, resources.size());
    return resources;
  }

  private RegistryResourceApi getRegistryResourceApi(final String registryName) {
    final Registry registry = registriesStore.getRegistry(registryName);
    final String format = registry.getFormat();
    final RegistryResourceApi registryResourceApi = registryResourceApis.get(format);
    if (registryResourceApi == null) {
      throw new BadRequestException("Server is misconfigured");
    }
    return registryResourceApi;
  }
}
