package com.itesoft.registree.oci.rest.group;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.java.CheckedBiFunction;
import com.itesoft.registree.java.CheckedConsumer;
import com.itesoft.registree.oci.dto.json.CatalogDto;
import com.itesoft.registree.oci.dto.json.RepositoryTagsDto;
import com.itesoft.registree.oci.rest.AbstractRegistryRepositoryManager;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.OciRegistryRepositoryManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupRegistryRepositoryManager extends AbstractRegistryRepositoryManager implements OciRegistryRepositoryManager {
  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public RegistryType getType() {
    return RegistryType.GROUP;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getCatalog(final OciOperationContext context,
                                                          final HttpServletRequest request)
    throws Exception {
    final CatalogDto catalogDto = new CatalogDto();

    perform(context,
            request,
            (registryRepositoryManager, subContext) -> {
              return registryRepositoryManager.getCatalog(subContext, request);
            },
            bytes -> {
              final CatalogDto subCatalogDto = objectMapper.readValue(bytes, CatalogDto.class);
              catalogDto.setRepositories(subCatalogDto.getRepositories());
            });

    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, catalogDto);
    };

    return ResponseEntity.ok(stream);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getTags(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String name)
    throws Exception {
    final Set<String> tags = new HashSet<>();
    perform(context,
            request,
            (registryRepositoryManager, subContext) -> {
              return registryRepositoryManager.getTags(subContext, request, name);
            },
            bytes -> {
              final RepositoryTagsDto subRepositoryTagsDto = objectMapper.readValue(bytes, RepositoryTagsDto.class);
              tags.addAll(subRepositoryTagsDto.getTags());
            });

    if (tags.isEmpty()) {
      // TODO: check if repository exists in any group, if not, return 404
    }

    final List<String> orderedTags = new ArrayList<>(tags);
    orderElements(orderedTags);

    final RepositoryTagsDto repositoryTagsDto = new RepositoryTagsDto();
    repositoryTagsDto.setName(name);
    repositoryTagsDto.setTags(orderedTags);

    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, repositoryTagsDto);
    };

    return ResponseEntity.ok(stream);
  }

  private void perform(final OciOperationContext context,
                       final HttpServletRequest request,
                       final CheckedBiFunction<OciRegistryRepositoryManager, OciOperationContext, ResponseEntity<StreamingResponseBody>> operation,
                       final CheckedConsumer<byte[], IOException> consumer)
    throws Exception {
    final GroupRegistry groupOciRegistry = (GroupRegistry) context.getRegistry();
    for (final String member : groupOciRegistry.getMemberNames()) {
      final OciOperationContext subContext = context.createSubContext(member);
      final OciRegistryRepositoryManager registryRepositoryManager = subContext.getRegistryManager(OciRegistryRepositoryManager.class);
      final ResponseEntity<StreamingResponseBody> response = operation.apply(registryRepositoryManager, subContext);
      if (!HttpStatus.OK.equals(response.getStatusCode())) {
        continue;
      }
      final byte[] bytes;
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        response.getBody().writeTo(baos);
        bytes = baos.toByteArray();
      }

      consumer.accept(bytes);
    }
  }
}
