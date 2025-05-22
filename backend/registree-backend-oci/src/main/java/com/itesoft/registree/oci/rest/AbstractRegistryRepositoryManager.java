package com.itesoft.registree.oci.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.itesoft.registree.oci.dto.json.CatalogDto;
import com.itesoft.registree.oci.storage.RepositoryStorage;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public abstract class AbstractRegistryRepositoryManager {
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private RepositoryStorage repositoryStorage;

  public ResponseEntity<StreamingResponseBody> getCatalog(final OciOperationContext context) throws IOException {
    final List<String> repositories = getRepositories(context);
    final CatalogDto catalogDto = new CatalogDto();
    catalogDto.setRepositories(repositories);
    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, catalogDto);
    };

    return ResponseEntity.ok(stream);
  }

  public List<String> getRepositories(final OciOperationContext context) throws IOException {
    if (!storageHelper.getDoStore(context.getRegistry())) {
      return Collections.emptyList();
    }
    final List<String> repositories = repositoryStorage.getRepositories(context.getRegistry());
    return getNullOrOrderElements(repositories);
  }

  public List<String> getTags(final OciOperationContext context,
                              final String name) {
    if (!storageHelper.getDoStore(context.getRegistry())) {
      return Collections.emptyList();
    }
    final List<String> tags = repositoryStorage.getTags(context.getRegistry(), name);
    return getNullOrOrderElements(tags);
  }

  public void orderElements(final List<String> elements) {
    elements.sort(new Comparator<String>() {
      @Override
      public int compare(final String o1, final String o2) {
        return o1.toLowerCase().compareTo(o2.toLowerCase());
      }
    });
  }

  private List<String> getNullOrOrderElements(final List<String> elements) {
    if (elements == null) {
      return null;
    }
    orderElements(elements);
    return elements;
  }
}
