package com.itesoft.registree.raw.rest.group;

import com.itesoft.registree.dto.GroupRegistry;
import com.itesoft.registree.java.CheckedBiFunction;
import com.itesoft.registree.raw.rest.RawFileManager;
import com.itesoft.registree.raw.rest.RawOperationContext;
import com.itesoft.registree.raw.rest.error.RawErrorManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class RawGroupRegistryHelper {
  @Autowired
  private RawErrorManager errorManager;

  public ResponseEntity<StreamingResponseBody>
  findAnswer(final RawOperationContext context,
             final CheckedBiFunction<RawOperationContext, RawFileManager, ResponseEntity<StreamingResponseBody>> function,
             final String errorMessage)
    throws Exception {
    final GroupRegistry groupRegistry = (GroupRegistry) context.getRegistry();
    for (final String member : groupRegistry.getMemberNames()) {
      final RawOperationContext subContext = context.createSubContext(member);
      final RawFileManager fileManager = subContext.getFileManager();
      final ResponseEntity<StreamingResponseBody> response = function.apply(subContext, fileManager);
      if (HttpStatus.OK.equals(response.getStatusCode())) {
        return response;
      }
    }

    return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                         errorMessage);
  }
}
