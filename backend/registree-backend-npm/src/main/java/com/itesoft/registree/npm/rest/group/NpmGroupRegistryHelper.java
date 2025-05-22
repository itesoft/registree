package com.itesoft.registree.npm.rest.group;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.java.CheckedBiFunction;
import com.itesoft.registree.npm.rest.NpmOperationContext;
import com.itesoft.registree.npm.rest.NpmPackageManager;
import com.itesoft.registree.npm.rest.error.NpmErrorManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class NpmGroupRegistryHelper {
  @Autowired
  private NpmErrorManager errorManager;

  public ResponseEntity<StreamingResponseBody>
  findAnswer(final NpmOperationContext context,
             final CheckedBiFunction<NpmOperationContext, NpmPackageManager, ResponseEntity<StreamingResponseBody>> function,
             final String errorMessage)
    throws Exception {
    final GroupRegistry groupRegistry = (GroupRegistry) context.getRegistry();
    for (final String member : groupRegistry.getMemberNames()) {
      final NpmOperationContext subContext = context.createSubContext(member);
      final NpmPackageManager packageManager = subContext.getPackageManager();
      final ResponseEntity<StreamingResponseBody> response = function.apply(subContext, packageManager);
      if (HttpStatus.OK.equals(response.getStatusCode())
        || HttpStatus.NOT_MODIFIED.equals(response.getStatusCode())) {
        return response;
      }
    }

    return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                         errorMessage);
  }
}
