package com.itesoft.registree.console.command.registry;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.RegistryController;
import com.itesoft.registree.dto.UpdateRegistryArgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "registry-update",
         description = "Updates a registry")
public class RegistryUpdateCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The name of the registry to update")
  private String name;

  @Option(names = "-c",
          description = "The configuration as JSON")
  private String configuration;

  @Spec
  private CommandSpec spec;

  @Autowired
  private RegistryController registryController;

  @Override
  public Integer call() throws Exception {
    final UpdateRegistryArgs updateRegistryArgs =
      UpdateRegistryArgs.builder()
        .configuration(configuration)
        .build();
    registryController.updateRegistry(RequestContext.builder().build(),
                                      ResponseContext.builder().build(),
                                      name,
                                      updateRegistryArgs);
    spec.commandLine().getOut().println("Registry updated");
    return 0;
  }
}
