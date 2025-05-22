package com.itesoft.registree.console;

import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

public class ExecutionExceptionHandler implements IExecutionExceptionHandler {
  private Exception exception;

  @Override
  public int handleExecutionException(final Exception exception,
                                      final CommandLine commandLine,
                                      final ParseResult fullParseResult)
    throws Exception {
    this.exception = exception;
    return 1;
  }

  public Exception getException() {
    return exception;
  }
}
