package com.itesoft.registree.console.command.registry;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.console.command.AbstractGetCommand;
import com.itesoft.registree.controller.RegistryController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "registry-get",
         description = "Gets a registry")
public class RegistryGetCommand extends AbstractGetCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The name of the registry")
  private String name;

  @Autowired
  private RegistryController registryController;

  @Override
  public Integer call() throws Exception {
    doCall("Registry",
           () -> registryController.getRegistry(RequestContext.builder().build(),
                                                ResponseContext.builder().build(),
                                                name));
    return 0;
  }
}
