package com.itesoft.registree.console.command.registry;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.RegistryController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "registry-delete",
         description = "Deletes a registry")
public class RegistryDeleteCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The name of the registry")
  private String name;

  @Spec
  private CommandSpec spec;

  @Autowired
  private RegistryController registryController;

  @Override
  public Integer call() throws Exception {
    registryController.deleteRegistry(RequestContext.builder().build(),
                                      ResponseContext.builder().build(),
                                      name,
                                      null);
    spec.commandLine().getOut().println("Registry deleted");
    return 0;
  }
}
