package com.itesoft.registree.console;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.dto.ExecuteCommandArgs;
import com.itesoft.registree.console.dto.ExecuteCommandResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi;

@Component
public class CommandController {
  private static final Logger LOGGER = LoggerFactory.getLogger(CommandController.class);

  @Autowired
  private Commands commands;

  @Autowired
  private PicocliSpringFactory factory;

  public ExecuteCommandResult execute(final RequestContext requestContext,
                                      final ResponseContext responseContext,
                                      final ExecuteCommandArgs args) {
    return execute(requestContext,
                   responseContext,
                   args.getCommand(),
                   args.getArguments());
  }

  public ExecuteCommandResult execute(final RequestContext requestContext,
                                      final ResponseContext responseContext,
                                      @NotNull final String command,
                                      final List<String> arguments) {
    return execute(requestContext,
                   responseContext,
                   command,
                   arguments == null ? null : arguments.toArray(new String[arguments.size()]));
  }

  public ExecuteCommandResult execute(final RequestContext requestContext,
                                      final ResponseContext responseContext,
                                      @NotNull final String command,
                                      final String... arguments) {
    final RegistreeCommand registreeCommand = commands.getCommand(command);
    if (registreeCommand == null) {
      return ExecuteCommandResult.builder()
        .exitCode(1)
        .output(String.format("%s: command not found", command))
        .build();
    }
    final String[] notNullArguments = arguments == null ? new String[0] : arguments;
    final ParameterExceptionHandler parameterExceptionHandler = new ParameterExceptionHandler();
    final ExecutionExceptionHandler executionExceptionHandler = new ExecutionExceptionHandler();
    final StringWriter stringWriter = new StringWriter();
    final PrintWriter outputWriter = new PrintWriter(stringWriter);

    final int exitCode = new CommandLine(registreeCommand.getClass(), factory)
      .setParameterExceptionHandler(parameterExceptionHandler)
      .setExecutionExceptionHandler(executionExceptionHandler)
      .setColorScheme(new CommandLine.Help.ColorScheme.Builder().ansi(Ansi.OFF).build())
      .setErr(outputWriter)
      .setOut(outputWriter)
      .execute(notNullArguments);

    String output = stringWriter.toString();
    if (exitCode != 0) {
      Exception exception = parameterExceptionHandler.getException();
      if (exception != null) {
        output = exception.getMessage();
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug(exception.getMessage(), exception);
        }
      } else {
        exception = executionExceptionHandler.getException();
        if (exception != null) {
          output = exception.getMessage();
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(exception.getMessage(), exception);
          }
        }
      }
    }

    return ExecuteCommandResult.builder()
      .exitCode(exitCode)
      .output(output)
      .build();
  }
}
