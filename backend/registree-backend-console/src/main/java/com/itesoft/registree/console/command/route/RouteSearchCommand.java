package com.itesoft.registree.console.command.route;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.console.command.AbstractSearchCommand;
import com.itesoft.registree.controller.RouteController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Component
@Command(name = "route-search",
         description = "Searches routes")
public class RouteSearchCommand extends AbstractSearchCommand implements RegistreeCommand {
  @Spec
  private CommandSpec spec;

  @Autowired
  private RouteController routeController;

  @Override
  public Integer call() throws Exception {
    doCall("routes",
           () -> routeController.searchRoutes(RequestContext.builder().build(),
                                              ResponseContext.builder().build(),
                                              filter,
                                              sort,
                                              page,
                                              pageSize));
    return 0;
  }
}
