package com.itesoft.registree.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class CommandExecutionHelper {
  private static class StreamReaderThread extends Thread {
    private final Process process;
    private final Consumer<String> lineConsumer;

    StreamReaderThread(final Process process,
                       final Consumer<String> lineConsumer) {
      this.process = process;
      this.lineConsumer = lineConsumer;
    }

    @Override
    public void run() {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line = null;
        while ((line = reader.readLine()) != null) {
          lineConsumer.accept(line);
        }
      } catch (final Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  public static void execute(final String... command) throws Exception {
    execute(0, null, null, command);
  }

  public static void execute(final int expectedRetCode,
                             final String errorMessageExtract,
                             final Integer fromEndErrorLine,
                             final String... command)
    throws Exception {
    final StringBuilder commandAsString = new StringBuilder(">");
    for (final String param : command) {
      commandAsString.append(" ");
      commandAsString.append(param);
    }
    System.out.println(commandAsString);

    final Process process = new ProcessBuilder().command(command).redirectErrorStream(true).start();
    final List<String> outputLines = new ArrayList<>();
    final Thread ioThread = new StreamReaderThread(process,
                                                   (line) -> {
                                                     outputLines.add(line);
                                                     System.out.println(line);
                                                   });
    ioThread.start();

    assertEquals(expectedRetCode,
                 process.waitFor(),
                 "Execution failed, command: " + Arrays.asList(command));
    ioThread.join();
    if (expectedRetCode != 0) {
      final String errorLine = outputLines.get(outputLines.size() - fromEndErrorLine);
      assertThat(errorLine.toString().toLowerCase()).contains(errorMessageExtract.toLowerCase());
    }
  }

  public static String executeGetOutput(final String... command) throws IOException, InterruptedException {
    final Process process = new ProcessBuilder()
      .command(command)
      .redirectErrorStream(true)
      .start();

    final StringBuilder result = new StringBuilder();
    final Thread ioThread = new StreamReaderThread(process,
                                                   (line) -> {
                                                     result.append(line);
                                                   });
    ioThread.start();


    assertEquals(0,
                 process.waitFor());
    ioThread.join();
    return result.toString();
  }

  private CommandExecutionHelper() {
  }
}
