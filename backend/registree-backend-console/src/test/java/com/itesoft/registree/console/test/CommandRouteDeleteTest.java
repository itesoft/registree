package com.itesoft.registree.console.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.CommandController;
import com.itesoft.registree.console.dto.ExecuteCommandResult;
import com.itesoft.registree.controller.RouteController;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.controller.UserRouteController;
import com.itesoft.registree.dto.CreateRouteArgs;
import com.itesoft.registree.dto.CreateUserArgs;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.Route;
import com.itesoft.registree.dto.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CommandRouteDeleteTest {
  @Autowired
  private CommandController commandController;

  @Autowired
  private UserController userController;

  @Autowired
  private RouteController routeController;

  @Autowired
  private UserRouteController userRouteController;

  @Test
  public void deleteRoute() {
    final String username = "deleteRoute";
    final String path = "/path/to/resource";
    final String permissions = "rw";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder()
      .username(username)
      .password("pass")
      .build();
    final User user =
      userController.createUser(RequestContext.builder().build(),
                                ResponseContext.builder().build(),
                                createUserArgs);

    final CreateRouteArgs createRouteArgs = CreateRouteArgs.builder()
      .permissions(permissions)
      .build();

    userRouteController.createRoute(RequestContext.builder().build(),
                                    ResponseContext.builder().build(),
                                    OneOfLongOrString.from(user.getId()),
                                    path,
                                    createRouteArgs);

    List<Route> routes = routeController.searchRoutes(RequestContext.builder().build(),
                                                      ResponseContext.builder().build(),
                                                      "user.id==" + user.getId(),
                                                      null,
                                                      null,
                                                      null);
    assertEquals(1, routes.size());
    final Route route = routes.get(0);
    assertEquals(user.getId(), route.getUserIdentifier().getId());
    assertEquals(user.getUsername(), route.getUserIdentifier().getUsername());
    assertEquals(path, route.getPath());
    assertEquals(permissions, route.getPermissions());

    final ExecuteCommandResult executeCommandResult = commandController.execute(RequestContext.builder().build(),
                                                                                ResponseContext.builder().build(),
                                                                                "route-delete",
                                                                                username,
                                                                                path);
    assertEquals(0, executeCommandResult.getExitCode());

    routes = routeController.searchRoutes(RequestContext.builder().build(),
                                          ResponseContext.builder().build(),
                                          "user.id==" + user.getId(),
                                          null,
                                          null,
                                          null);
    assertEquals(0, routes.size());
  }
}
