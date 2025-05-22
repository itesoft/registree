package com.itesoft.registree.oci.api;

import static com.itesoft.registree.oci.config.OciConstants.FORMAT;
import static com.itesoft.registree.oci.storage.Constant.BLOB_FILE_PATH;
import static com.itesoft.registree.oci.storage.Constant.DATA_FILE_NAME;
import static com.itesoft.registree.oci.storage.Constant.REPOSITORY_TAG_FILE_PATH;

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
import com.itesoft.registree.oci.config.OciRegistries;
import com.itesoft.registree.registry.api.AbstractRegistryResourceApi;
import com.itesoft.registree.registry.api.RegistryResourceApi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OciRegistryResourceApi extends AbstractRegistryResourceApi implements RegistryResourceApi {
  private enum ResourceKind {
    BLOB,
    REPOSITORY
  }

  @Autowired
  private OciRegistries ociRegistries;

  @Override
  public String getFormat() {
    return FORMAT;
  }

  @Override
  protected String getRootSubPath() {
    return "/v2";
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
    final ResourceKind resourceKind;
    if (subPath == null) {
      searchPath = rootPath;
      resourceKind = null;
    } else {
      if (subPath.startsWith("blobs")) {
        resourceKind = ResourceKind.BLOB;
      } else {
        resourceKind = ResourceKind.REPOSITORY;
      }
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

        if (fileName.startsWith("_")
          && !fileName.equals("_manifests")) {
          return;
        }

        final Path relativePath = rootPath.relativize(res);
        final String relativePathAsString = relativePath.toString();

        final Path parentPath = relativePath.getParent();
        String parentPathAsString = null;
        if (parentPath != null) {
          final String parentName = parentPath.getFileName().toString();
          if (parentName.equals("_manifests")
            && fileName.equals("revisions")) {
            return;
          }
          parentPathAsString = parentPath.toString();
        }

        String type = DIRECTORY_TYPE;
        String relativeDownloadPath = null;
        String componentGav = null;
        String filePath = null;
        if (parentPath != null) {
          if (ResourceKind.BLOB.equals(resourceKind) && Files.isRegularFile(Paths.get(res.toString(), DATA_FILE_NAME))) { // virtual blob file
            type = FILE_TYPE;

            final String digest = String.format("sha256:%s", fileName);
            relativeDownloadPath = String.format("v2/blobs/%s", digest);
            filePath = String.format(BLOB_FILE_PATH, digest);
          } else {
            final Path parentParentPath = parentPath.getParent();
            String twoLevelParents = null;
            if (parentParentPath != null) {
              twoLevelParents = String.format("%s/%s", parentParentPath.getFileName(), parentPath.getFileName());
            }

            if (twoLevelParents != null && twoLevelParents.equals("_manifests/tags")) { // virtual repository manifest file
              type = FILE_TYPE;

              // from repositories/alpine/curl/_manifests/tags/latest to alpine/curl
              final String tmp = relativePathAsString.substring("repositories/".length());
              final String name = tmp.substring(0, tmp.length() - ("/_manifests/tags/" + fileName).length());
              final String tag = fileName;

              componentGav = Gav.builder()
                .name(name)
                .version(tag)
                .build()
                .toString();

              relativeDownloadPath = String.format("v2/repositories/%s/manifests/%s", name, tag);
              filePath = String.format(REPOSITORY_TAG_FILE_PATH, name, tag);
            }
          }
        }

        final Resource resource =
          Resource.builder()
            .name(fileName)
            .path(relativePathAsString)
            .parentPath(parentPathAsString)
            .type(type)
            .relativeDownloadPath(relativeDownloadPath)
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
    final Registry registry = ociRegistries.getRegistry(registryName);
    if (registry == null) {
      throw new NotFoundException(String.format("Registry %s cannot be found", registryName));
    }
    return registry;
  }
}
