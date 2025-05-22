package com.itesoft.registree.console.command.registry;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.console.command.AbstractSearchCommand;
import com.itesoft.registree.controller.RegistryController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "registry-search",
         description = "Searches registries")
public class RegistrySearchCommand extends AbstractSearchCommand implements RegistreeCommand {
  @Autowired
  private RegistryController registryController;

  @Override
  public Integer call() throws Exception {
    doCall("registries",
           () -> registryController.searchRegistries(RequestContext.builder().build(),
                                                     ResponseContext.builder().build(),
                                                     filter,
                                                     sort,
                                                     page,
                                                     pageSize));
    return 0;
  }
}
