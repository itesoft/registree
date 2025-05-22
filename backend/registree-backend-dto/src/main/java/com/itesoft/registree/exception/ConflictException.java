package com.itesoft.registree.exception;

/**
 * Exception thrown when a conflict exists.
 */
public class ConflictException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with null as its detail message.
   */
  public ConflictException() {
  }

  /**
   * Constructs a new exception with the specified detail message. The cause is
   * not initialized, and may subsequently be initialized by a call to
   * {@link Throwable#initCause(Throwable)}.
   *
   * @param message the detail message. The detail message is saved for later
   *                retrieval by the {@link Throwable#getMessage()} method.
   */
  public ConflictException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause. Note
   * that the detail message associated with cause is not automatically
   * incorporated in this exception's detail message.
   *
   * @param message - the detail message (which is saved for later retrieval by
   *                the {@link Throwable#getMessage()} method).
   * @param cause   - the cause (which is saved for later retrieval by the
   *                {@link Throwable#getCause()} method). (A null value is
   *                permitted, and indicates that the cause is nonexistent or
   *                unknown.)
   */
  public ConflictException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
