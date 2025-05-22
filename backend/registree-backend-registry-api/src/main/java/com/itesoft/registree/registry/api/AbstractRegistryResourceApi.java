package com.itesoft.registree.registry.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.Resource;
import com.itesoft.registree.exception.BadRequestException;
import com.itesoft.registree.exception.NotFoundException;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractRegistryResourceApi implements RegistryResourceApi {
  @Autowired
  private StorageHelper storageHelper;

  @Override
  public List<Resource> getRootResources(final String registryName) {
    final Registry registry = getRegistry(registryName);
    return getResources(registry, null);
  }

  @Override
  public List<Resource> getResources(final String registryName,
                                     final String path) {
    final Registry registry = getRegistry(registryName);
    return getResources(registry, path);
  }

  protected abstract Registry getRegistry(String registryName);

  protected abstract String getRootSubPath();

  protected abstract boolean doFillResources(List<Resource> resources,
                                             Set<String> resourceNames,
                                             String sourceRegistryName,
                                             String rootPathAsString,
                                             String subPath)
    throws Exception;

  private List<Resource> getResources(final Registry registry,
                                      final String path) {
    final List<Resource> resources = new ArrayList<>();
    doFillResources(resources, registry, path);
    resources.sort((first, second) -> {
      if (DIRECTORY_TYPE.equals(first.getType())
        && FILE_TYPE.equals(second.getType())) {
        return -1;
      }
      if (FILE_TYPE.equals(first.getType())
        && DIRECTORY_TYPE.equals(second.getType())) {
        return 1;
      }
      return first.getName().compareTo(second.getName());
    });
    return resources;
  }

  private void doFillResources(final List<Resource> resources,
                               final Registry registry,
                               final String path) {
    final Set<String> resourceNames = new HashSet<>();
    final boolean found = doFillResources(resources, resourceNames, registry, path);
    if (!found) {
      throw new NotFoundException(String.format("Resource %s cannot be found on registry", path, registry.getName()));
    }
  }

  private boolean doFillResources(final List<Resource> resources,
                                  final Set<String> resourceNames,
                                  final Registry registry,
                                  final String path) {
    if (registry instanceof final GroupRegistry groupRegistry) {
      boolean found = false;
      for (final String memberName : groupRegistry.getMemberNames()) {
        final Registry subRegistry = getRegistry(memberName);
        found |= doFillResources(resources, resourceNames, subRegistry, path);
      }
      return found;
    } else {
      final String rootPathAsString = storageHelper.getStoragePath(registry) + getRootSubPath();
      return doFillResourcesNoException(resources, resourceNames, registry.getName(), rootPathAsString, path);
    }
  }

  private boolean doFillResourcesNoException(final List<Resource> resources,
                                             final Set<String> resourceNames,
                                             final String sourceRegistryName,
                                             final String rootPathAsString,
                                             final String subPath) {
    try {
      return doFillResources(resources,
                             resourceNames,
                             sourceRegistryName,
                             rootPathAsString,
                             subPath);
    } catch (final Exception exception) {
      throw new BadRequestException("Something went wrong", exception);
    }
  }
}
