package com.itesoft.registree.maven.rest.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class MavenErrorManager {
  @Autowired
  private ObjectMapper objectMapper;

  public ResponseEntity<StreamingResponseBody> getErrorResponse(final HttpStatus httpStatus,
                                                                final String message) {
    return getErrorResponse(httpStatus, null, message);
  }

  public ResponseEntity<StreamingResponseBody> getErrorResponse(final HttpStatus httpStatus,
                                                                final HttpHeaders headers,
                                                                final String message) {
    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, message);
    };

    return ResponseEntity.status(httpStatus)
      .headers(headers)
      .body(stream);
  }
}
