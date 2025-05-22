package com.itesoft.registree.console.command.route;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.UserRouteController;
import com.itesoft.registree.dto.OneOfLongOrString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "route-delete",
         description = "Deletes a route")
public class RouteDeleteCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The id or name of the user the route will be deleted from")
  private String userIdOrName;

  @Parameters(index = "1",
              description = "The path of the route")
  private String routePath;

  @Spec
  private CommandSpec spec;

  @Autowired
  private UserRouteController userRouteController;

  @Override
  public Integer call() throws Exception {
    userRouteController.deleteRoute(RequestContext.builder().build(),
                                    ResponseContext.builder().build(),
                                    OneOfLongOrString.valueOf(userIdOrName),
                                    routePath,
                                    null);
    spec.commandLine().getOut().println("Route deleted");
    return 0;
  }
}
