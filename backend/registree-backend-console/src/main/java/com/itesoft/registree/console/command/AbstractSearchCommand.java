package com.itesoft.registree.console.command;

import java.io.PrintWriter;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

public abstract class AbstractSearchCommand {
  // CHECKSTYLE:OFF
  @Option(names = "-f",
          description = "The search filter")
  protected String filter;

  @Option(names = "-s",
          description = "The search sort")
  protected String sort;

  @Option(names = "-p",
          description = "The page")
  protected Integer page;

  @Option(names = "-n",
          description = "The page size")
  protected Integer pageSize;
  // CHECKSTYLE:ON

  @Spec
  private CommandSpec spec;

  @Autowired
  private ObjectMapper objectMapper;

  protected <T> void doCall(final String label,
                            final Supplier<List<T>> searchPerformer)
    throws Exception {
    final PrintWriter out = spec.commandLine().getOut();
    final List<T> result = searchPerformer.get();
    out.println(String.format("Found %s:", label));
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, result);
  }
}
