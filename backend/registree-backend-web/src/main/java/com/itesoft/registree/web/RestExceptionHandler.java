package com.itesoft.registree.web;

import com.itesoft.registree.dto.ApiError;
import com.itesoft.registree.exception.ExceptionHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(RestExceptionHandler.class);

  private final ExceptionHelper exceptionHelper = ExceptionHelper.builder().build();

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> globalExceptionHandler(final Exception exception, final WebRequest request) {
    LOGGER.error(exception.getMessage(), exception); // TODO: limit logging ?
    final int errorCode = exceptionHelper.getErrorCodeFromThrowable(exception);
    return ResponseEntity.status(HttpStatusCode.valueOf(errorCode)).build();
    /* FIXME: before returning an ApiError, we must be sure we are returning a mime-type that supports objects
    return ResponseEntity.status(HttpStatusCode.valueOf(errorCode))
      .body(ApiError.builder().message(exception.getMessage()).build());
    */
  }
}
