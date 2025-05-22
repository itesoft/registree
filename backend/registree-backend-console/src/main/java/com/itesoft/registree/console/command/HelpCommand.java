package com.itesoft.registree.console.command;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.itesoft.registree.console.Commands;
import com.itesoft.registree.console.RegistreeCommand;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "help",
         helpCommand = true,
         description = "Get help")
public class HelpCommand implements RegistreeCommand {
  @Parameters(index = "0",
              paramLabel = "[<command>]",
              description = "Display usage of the given command",
              defaultValue = "")
  private String subCommand;

  @Spec
  private CommandSpec spec;

  @Autowired
  @Lazy
  private Commands commands;

  @Override
  public Integer call() throws Exception {
    final PrintWriter out = spec.commandLine().getOut();
    if (subCommand == null || subCommand.isEmpty()) {
      out.println("Available commands:");
      final List<String> commandNames = new ArrayList<>();
      for (final RegistreeCommand registreeCommand : commands.getCommands()) {
        final String commandName = new CommandLine(registreeCommand).getCommandName();
        commandNames.add(commandName);
      }
      commandNames.remove("help");
      Collections.sort(commandNames);
      for (final String commandName : commandNames) {
        out.println("  - " + commandName);
      }
      out.println("Use help <command> to display usage of the given command");
    } else {
      final RegistreeCommand registreeCommand = commands.getCommand(subCommand);
      if (registreeCommand == null) {
        out.println(String.format("Command '%s' does not exist, use help to see available commands",
                                  subCommand));
        return 1;
      }
      new CommandLine(registreeCommand).usage(out, Ansi.OFF);
    }
    return 0;
  }
}
