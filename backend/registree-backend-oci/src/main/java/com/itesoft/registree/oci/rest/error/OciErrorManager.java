package com.itesoft.registree.oci.rest.error;

import java.util.ArrayList;
import java.util.List;

import com.itesoft.registree.oci.dto.json.ErrorDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class OciErrorManager {
  @Autowired
  private ObjectMapper objectMapper;

  public ResponseEntity<StreamingResponseBody> getErrorResponse(final HttpStatus httpStatus,
                                                                final String code,
                                                                final String message) {
    return getErrorResponse(httpStatus, null, code, message);
  }

  public ResponseEntity<StreamingResponseBody> getErrorResponse(final HttpStatus httpStatus,
                                                                final HttpHeaders headers,
                                                                final String code,
                                                                final String message) {
    final List<ErrorDto> errors = new ArrayList<>();
    final ErrorDto error = new ErrorDto();
    error.setCode(code);
    error.setMessage(message);

    final StreamingResponseBody stream = outputStream -> {
      objectMapper.writeValue(outputStream, errors);
    };

    return ResponseEntity.status(httpStatus)
      .headers(headers)
      .body(stream);
  }
}
