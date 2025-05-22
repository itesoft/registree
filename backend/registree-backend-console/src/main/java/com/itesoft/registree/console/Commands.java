package com.itesoft.registree.console;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

@Component
public class Commands {
  private final Map<String, RegistreeCommand> commands = new HashMap<>();

  @Autowired
  private void commands(final List<RegistreeCommand> commands) {
    for (final RegistreeCommand command : commands) {
      this.commands.put(new CommandLine(command).getCommandName(), command);
    }
  }

  public RegistreeCommand getCommand(final String command) {
    return commands.get(command);
  }

  public Collection<RegistreeCommand> getCommands() {
    return commands.values();
  }
}
