package com.itesoft.registree.console.command;

import java.io.PrintWriter;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

public class AbstractGetCommand {
  @Spec
  private CommandSpec spec;

  @Autowired
  private ObjectMapper objectMapper;

  protected <T> void doCall(final String label,
                            final Supplier<T> getPerformer)
    throws Exception {
    final PrintWriter out = spec.commandLine().getOut();
    final T result = getPerformer.get();
    out.println(String.format("%s details:", label));
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(out, result);
  }
}
