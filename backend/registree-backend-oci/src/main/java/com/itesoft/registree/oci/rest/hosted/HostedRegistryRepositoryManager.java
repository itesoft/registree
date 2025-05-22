package com.itesoft.registree.oci.rest.hosted;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.dto.json.RepositoryTagsDto;
import com.itesoft.registree.oci.rest.AbstractRegistryRepositoryManager;
import com.itesoft.registree.oci.rest.OciRegistryRepositoryManager;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.error.ErrorCode;
import com.itesoft.registree.oci.rest.error.OciErrorManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class HostedRegistryRepositoryManager extends AbstractRegistryRepositoryManager implements OciRegistryRepositoryManager {
  @Autowired
  private OciErrorManager errorManager;

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public RegistryType getType() {
    return RegistryType.HOSTED;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getCatalog(final OciOperationContext context,
                                                          final HttpServletRequest request)
    throws Exception {
    return getCatalog(context);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getTags(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String name)
    throws Exception {
    final List<String> tags = getTags(context, name);
    if (tags == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           ErrorCode.NAME_UNKNOWN,
                                           String.format("Cannot find repository with name %s", name));
    }

    orderElements(tags);

    final RepositoryTagsDto repositoryTags = new RepositoryTagsDto();
    repositoryTags.setName(name);
    repositoryTags.setTags(tags);
    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, repositoryTags);
    };

    return ResponseEntity.ok(stream);
  }
}
