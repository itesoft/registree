package com.itesoft.registree.maven.api;

import static com.itesoft.registree.maven.config.MavenConstants.FORMAT;
import static com.itesoft.registree.maven.config.MavenConstants.METADATA_FILE_NAME;

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
import com.itesoft.registree.maven.config.MavenRegistries;
import com.itesoft.registree.registry.api.AbstractRegistryResourceApi;
import com.itesoft.registree.registry.api.RegistryResourceApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MavenRegistryResourceApi extends AbstractRegistryResourceApi implements RegistryResourceApi {
  private static final String DIRECTORY_TYPE = "directory";
  private static final String FILE_TYPE = "file";

  @Autowired
  private MavenRegistries mavenRegistries;

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
        final Path relativeParentPath = relativePath.getParent();
        final String relativePathAsString = relativePath.toString();

        String downloadPath = null;
        String componentGav = null;
        String filePath = null;
        final String type;
        if (Files.isDirectory(res)) {
          type = DIRECTORY_TYPE;
          final Path parentPath = res.getParent();
          final Path parentMetadata = Paths.get(parentPath.toString(), METADATA_FILE_NAME);
          if (Files.isRegularFile(parentMetadata)) {
            final Path parentParentPath = parentPath.getParent();
            final Path groupPath = rootPath.relativize(parentParentPath);
            final String groupId = groupPath.toString().replace("/", ".");
            componentGav = Gav.builder()
              .group(groupId)
              .name(parentPath.getFileName().toString())
              .version(fileName)
              .build()
              .toString();
          }
        } else {
          type = FILE_TYPE;
          downloadPath = relativePathAsString;
          if (!fileName.startsWith(METADATA_FILE_NAME)) {
            filePath = relativePathAsString;
          }
        }

        final Resource resource =
          Resource.builder()
            .name(fileName)
            .path(relativePathAsString)
            .parentPath(relativeParentPath == null ? null : relativeParentPath.toString())
            .type(type)
            .relativeDownloadPath(downloadPath)
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
    final Registry registry = mavenRegistries.getRegistry(registryName);
    if (registry == null) {
      throw new NotFoundException(String.format("Registry %s cannot be found", registryName));
    }
    return registry;
  }
}
