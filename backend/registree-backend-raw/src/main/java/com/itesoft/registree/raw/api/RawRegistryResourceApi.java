package com.itesoft.registree.raw.api;

import static com.itesoft.registree.raw.config.RawConstants.FORMAT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import com.itesoft.registree.dto.Gav;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.Resource;
import com.itesoft.registree.exception.NotFoundException;
import com.itesoft.registree.raw.config.RawRegistries;
import com.itesoft.registree.registry.api.AbstractRegistryResourceApi;
import com.itesoft.registree.registry.api.RegistryResourceApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RawRegistryResourceApi extends AbstractRegistryResourceApi implements RegistryResourceApi {
  private static final String DIRECTORY_TYPE = "directory";
  private static final String FILE_TYPE = "file";

  @Autowired
  private RawRegistries rawRegistries;

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  protected String getRootSubPath() {
    return "";
  }

  @Override
  protected boolean doFillResources(final List<Resource> resources,
                                    final Set<String> resourceNames,
                                    final String sourceRegistryName,
                                    final String rootPathAsString,
                                    final String subPath)
    throws IOException {
    final Path rootPath = Paths.get(rootPathAsString);
    final Path searchPath;
    if (subPath == null) {
      searchPath = rootPath;
    } else {
      searchPath = Paths.get(rootPathAsString,
                             subPath);
    }

    if (!Files.isDirectory(searchPath)) {
      return false;
    }

    Files.newDirectoryStream(searchPath)
      .forEach(res -> {
        final String fileName = res.getFileName().toString();
        if (resourceNames.contains(fileName)) {
          return;
        }
        resourceNames.add(fileName);

        final Path relativePath = rootPath.relativize(res);
        final Path parentPath = relativePath.getParent();
        final String relativePathAsString = relativePath.toString();

        String componentGav = null;
        String filePath = null;
        final String type;
        if (Files.isDirectory(res)) {
          type = DIRECTORY_TYPE;
        } else {
          type = FILE_TYPE;
          componentGav = Gav.builder()
            .name(relativePathAsString)
            .build()
            .toString();
          filePath = relativePathAsString;
        }

        final Resource resource =
          Resource.builder()
            .name(fileName)
            .path(relativePathAsString)
            .parentPath(parentPath == null ? null : parentPath.toString())
            .type(type)
            .relativeDownloadPath(filePath)
            .sourceRegistryName(sourceRegistryName)
            .componentGav(componentGav)
            .filePath(filePath)
            .build();
        resources.add(resource);
      });

    return true;
  }

  @Override
  protected Registry getRegistry(final String registryName) {
    final Registry registry = rawRegistries.getRegistry(registryName);
    if (registry == null) {
      throw new NotFoundException(String.format("Registry %s cannot be found", registryName));
    }
    return registry;
  }
}
