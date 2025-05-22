package com.itesoft.registree.console.command.registry;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.RegistryController;
import com.itesoft.registree.dto.CreateRegistryArgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "registry-create",
         description = "Creates a registry")
public class RegistryCreateCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The format")
  private String format;

  @Parameters(index = "1",
              description = "The type")
  private String type;

  @Parameters(index = "2",
              description = "The name")
  private String name;

  @Option(names = "-c",
          description = "The configuration as JSON",
          defaultValue = "{}")
  private String configuration;

  @Spec
  private CommandSpec spec;

  @Autowired
  private RegistryController registryController;

  @Override
  public Integer call() throws Exception {
    final CreateRegistryArgs createRegistryArgs =
      CreateRegistryArgs.builder()
        .format(format)
        .type(type)
        .name(name)
        .configuration(configuration)
        .build();
    registryController.createRegistry(RequestContext.builder().build(),
                                      ResponseContext.builder().build(),
                                      createRegistryArgs);
    spec.commandLine().getOut().println("Registry created");
    return 0;
  }
}
