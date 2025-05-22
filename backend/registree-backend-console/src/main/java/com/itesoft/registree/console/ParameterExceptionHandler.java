package com.itesoft.registree.console;

import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.ParameterException;

public class ParameterExceptionHandler implements IParameterExceptionHandler {
  private Exception exception;

  @Override
  public int handleParseException(final ParameterException exception,
                                  final String[] args)
    throws Exception {
    this.exception = exception;
    return 2;
  }

  public Exception getException() {
    return exception;
  }
}
