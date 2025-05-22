package com.itesoft.registree.exception;

/**
 *
 * Helper for getting error code from exception and exception from error code
 */
public class ExceptionHelper {
  private static final int BAD_REQUEST_EXCEPTION_CODE = 400;
  private static final int UNAUTHORIZED_EXCEPTION_CODE = 401;
  private static final int FORBIDDEN_EXCEPTION_CODE = 403;
  private static final int NOT_FOUND_EXCEPTION_CODE = 404;
  private static final int CONFLICT_EXCEPTION_CODE = 409;
  private static final int UNPROCESSABLE_EXCEPTION_CODE = 422;
  private static final int DEFAULT_EXCEPTION_CODE = 500;

  /**
   * Exception Helper Builder.
   *
   */
  public static class Builder {
    /**
     * Build the ExceptionHelper.
     *
     * @return an instance of ExceptionHelper
     */
    public ExceptionHelper build() {
      return new ExceptionHelper(this);
    }
  }

  /**
   * Builder for the ExceptionHelper class.
   *
   * @return an instance of ExceptionHelper
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Default constructor
   */
  public ExceptionHelper() {
  }

  /**
   * Default constructor
   *
   * @param builder the ExceptionHelper builder
   */
  public ExceptionHelper(final Builder builder) {
    // no properties for now
  }

  /**
   * Returns the error code associated to the given throwable.
   *
   * @param exception the exception
   * @return an error code corresponding to the exception
   */
  public int getErrorCodeFromThrowable(final Throwable exception) {
    if (exception == null) {
      throw new RuntimeException("Unable to process a null exception");
    }
    if (exception instanceof BadRequestException) {
      return BAD_REQUEST_EXCEPTION_CODE;
    }
    if (exception instanceof UnauthorizedException) {
      return UNAUTHORIZED_EXCEPTION_CODE;
    }
    if (exception instanceof ForbiddenException) {
      return FORBIDDEN_EXCEPTION_CODE;
    }
    if (exception instanceof NotFoundException) {
      return NOT_FOUND_EXCEPTION_CODE;
    } else if (exception instanceof ConflictException) {
      return CONFLICT_EXCEPTION_CODE;
    } else if (exception instanceof UnprocessableException) {
      return UNPROCESSABLE_EXCEPTION_CODE;
    }

    return DEFAULT_EXCEPTION_CODE;
  }

  /**
   * Returns an instance of RuntimeException corresponding to the given error
   * code.
   *
   * @param errorCode the error code.
   * @param message   the message for the exception
   * @param cause     the exception cause if available
   * @return an instance of RuntimeException with the given message
   */
  public RuntimeException getRuntimeExceptionFromErrorCode(final int errorCode, final String message, final Exception cause) {
    final RuntimeException exception;
    switch (errorCode) {
    case BAD_REQUEST_EXCEPTION_CODE:
      exception = new BadRequestException(message, cause);
      break;
    case UNAUTHORIZED_EXCEPTION_CODE:
      exception = new UnauthorizedException(message, cause);
      break;
    case FORBIDDEN_EXCEPTION_CODE:
      exception = new ForbiddenException(message, cause);
      break;
    case NOT_FOUND_EXCEPTION_CODE:
      exception = new NotFoundException(message, cause);
      break;
    case CONFLICT_EXCEPTION_CODE:
      exception = new ConflictException(message, cause);
      break;
    case UNPROCESSABLE_EXCEPTION_CODE:
      exception = new UnprocessableException(message, cause);
      break;
    default:
      exception = new RuntimeException(message, cause);
    }

    return exception;
  }
}
