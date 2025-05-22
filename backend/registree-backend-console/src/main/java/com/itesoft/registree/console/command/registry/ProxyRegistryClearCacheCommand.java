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
@Command(name = "proxy-registry-clear-cache",
         description = "Clears a proxy registry cache")
public class ProxyRegistryClearCacheCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The name of the proxy registry to clear cache of")
  private String name;

  @Spec
  private CommandSpec spec;

  @Autowired
  private RegistryController registryController;

  @Override
  public Integer call() throws Exception {
    registryController.clearProxyRegistryCache(RequestContext.builder().build(),
                                               ResponseContext.builder().build(),
                                               name,
                                               null);
    spec.commandLine().getOut().println("Proxy registry cache cleared");
    return 0;
  }
}
