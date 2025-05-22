package com.itesoft.registree.maven.rest.group;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.java.CheckedBiFunction;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.rest.error.MavenErrorManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class MavenGroupRegistryHelper {
  @Autowired
  private MavenErrorManager errorManager;

  public <T> ResponseEntity<StreamingResponseBody> findAnswer(final MavenOperationContext context,
                                                              final CheckedBiFunction<MavenOperationContext, T, ResponseEntity<StreamingResponseBody>> function,
                                                              final String notFoundErrorMessage,
                                                              final Class<T> managerType)
    throws Exception {
    final GroupRegistry groupRegistry = (GroupRegistry) context.getRegistry();
    for (final String member : groupRegistry.getMemberNames()) {
      final MavenOperationContext subContext = context.createSubContext(member);
      final T mavenManager = subContext.getMavenManager(managerType);
      final ResponseEntity<StreamingResponseBody> response = function.apply(subContext, mavenManager);
      if (HttpStatus.OK.equals(response.getStatusCode())) {
        return response;
      }
    }

    return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                         notFoundErrorMessage);
  }
}
