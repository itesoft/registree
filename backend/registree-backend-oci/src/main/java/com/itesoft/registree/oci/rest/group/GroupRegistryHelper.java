package com.itesoft.registree.oci.rest.group;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.java.CheckedBiFunction;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.error.OciErrorManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupRegistryHelper {
  @Autowired
  private OciErrorManager errorManager;

  public <T> ResponseEntity<StreamingResponseBody> findAnswer(final OciOperationContext context,
                                                              final Class<T> registryManagerClass,
                                                              final CheckedBiFunction<OciOperationContext, T, ResponseEntity<StreamingResponseBody>> function,
                                                              final String errorCode,
                                                              final String errorMessage)
    throws Exception {
    final GroupRegistry groupRegistry = (GroupRegistry) context.getRegistry();
    for (final String member : groupRegistry.getMemberNames()) {
      final OciOperationContext subContext = context.createSubContext(member);
      final T registryManager = subContext.getRegistryManager(registryManagerClass);
      final ResponseEntity<StreamingResponseBody> response = function.apply(subContext, registryManager);
      if (HttpStatus.OK.equals(response.getStatusCode())) {
        return response;
      }
    }

    return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                         errorCode,
                                         errorMessage);
  }
}
