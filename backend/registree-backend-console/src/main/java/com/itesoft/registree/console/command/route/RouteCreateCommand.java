package com.itesoft.registree.console.command.route;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.UserRouteController;
import com.itesoft.registree.dto.CreateRouteArgs;
import com.itesoft.registree.dto.OneOfLongOrString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "route-create",
         description = "Creates a route")
public class RouteCreateCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The id or name of the user the route will be added to")
  private String userIdOrName;

  @Parameters(index = "1",
              description = "The path of the route")
  private String routePath;

  @Parameters(index = "2",
              description = """
                            The permissions, any combination of the following:
                            - r: read
                            - w: write
                            - d: delete
                            """)
  private String permissions;

  @Spec
  private CommandSpec spec;

  @Autowired
  private UserRouteController userRouteController;

  @Override
  public Integer call() throws Exception {
    final CreateRouteArgs createRouteArgs =
      CreateRouteArgs.builder()
        .permissions(permissions)
        .build();
    userRouteController.createRoute(RequestContext.builder().build(),
                                    ResponseContext.builder().build(),
                                    OneOfLongOrString.valueOf(userIdOrName),
                                    routePath,
                                    createRouteArgs);
    spec.commandLine().getOut().println("Route created");
    return 0;
  }
}
