package com.itesoft.registree.rest;

import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.ClientBuilder;

import com.itesoft.registree.dto.CreateRouteArgs;
import com.itesoft.registree.dto.CreateUserArgs;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.Route;
import com.itesoft.registree.dto.User;
import com.itesoft.registree.rest.test.UserRouteClient;

import org.apache.http.client.utils.URIBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserRouteRestControllerTest extends RestControllerTest {
  private static final String USERS_URL = API_URL_PREFIX + "/users";
  private static final String ROUTES_URL = API_URL_PREFIX + "/routes";

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  private UserRouteClient userRouteClient;

  @BeforeAll
  public void setup() {
    final ResteasyClient client = (ResteasyClient) ClientBuilder.newClient();
    final ResteasyWebTarget target = client.target(server + API_URL_PREFIX);
    userRouteClient = target.proxy(UserRouteClient.class);
  }

  @Test
  public void createRoute() throws Exception {
    final String username = "createRoute";
    final String password = "pass";
    final String path = "/path/to/repository";
    final String permissions = "rw";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    final User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    final CreateRouteArgs createRouteArgs = CreateRouteArgs.builder().permissions(permissions).build();
    Route route = userRouteClient.createRoute(OneOfLongOrString.from(username),
                                              path,
                                              createRouteArgs);
    assertEquals(user.getId(), route.getUserIdentifier().getId());
    assertEquals(user.getUsername(), route.getUserIdentifier().getUsername());
    assertEquals(path, route.getPath());
    assertEquals(permissions, route.getPermissions());

    final URI uri =
      new URIBuilder()
        .setScheme("http")
        .setHost("localhost")
        .setPort(port)
        .setPath(ROUTES_URL)
        .addParameter("filter", "user.id==" + user.getId())
        .build();

    final ResponseEntity<List<Route>> routesResponse =
      restTemplate.exchange(uri,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Route>>() {
                            });
    assertThat(routesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    final List<Route> routes = routesResponse.getBody();
    assertEquals(1, routes.size());
    route = routes.get(0);
    assertEquals(user.getId(), route.getUserIdentifier().getId());
    assertEquals(user.getUsername(), route.getUserIdentifier().getUsername());
    assertEquals(path, route.getPath());
    assertEquals(permissions, route.getPermissions());
  }

  @Test
  public void updateRoute() throws Exception {
    final String username = "updateRoute";
    final String password = "pass";
    final String path = "/path/to/repository";
    final String permissions = "rw";
    final String permissionsUpdate = "r";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    final User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    CreateRouteArgs createRouteArgs = CreateRouteArgs.builder().permissions(permissions).build();
    Route route = userRouteClient.createRoute(OneOfLongOrString.from(username),
                                              path,
                                              createRouteArgs);
    assertEquals(user.getId(), route.getUserIdentifier().getId());
    assertEquals(user.getUsername(), route.getUserIdentifier().getUsername());
    assertEquals(path, route.getPath());
    assertEquals(permissions, route.getPermissions());

    createRouteArgs = CreateRouteArgs.builder().permissions(permissionsUpdate).build();
    route = userRouteClient.createRoute(OneOfLongOrString.from(username),
                                        path,
                                        createRouteArgs);
    assertEquals(user.getId(), route.getUserIdentifier().getId());
    assertEquals(user.getUsername(), route.getUserIdentifier().getUsername());
    assertEquals(path, route.getPath());
    assertEquals(permissionsUpdate, route.getPermissions());

    final URI uri =
      new URIBuilder()
        .setScheme("http")
        .setHost("localhost")
        .setPort(port)
        .setPath(ROUTES_URL)
        .addParameter("filter", "user.id==" + user.getId())
        .build();

    final ResponseEntity<List<Route>> routesResponse =
      restTemplate.exchange(uri,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Route>>() {
                            });
    assertThat(routesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    final List<Route> routes = routesResponse.getBody();
    assertEquals(1, routes.size());
    route = routes.get(0);
    assertEquals(user.getId(), route.getUserIdentifier().getId());
    assertEquals(user.getUsername(), route.getUserIdentifier().getUsername());
    assertEquals(path, route.getPath());
    assertEquals(permissionsUpdate, route.getPermissions());
  }

  @Test
  public void deleteRoute() throws Exception {
    final String username = "deleteRoute";
    final String password = "pass";
    final String path = "/path/to/repository";
    final String permissions = "rw";

    final CreateUserArgs createUserArgs = CreateUserArgs.builder().username(username).password(password).build();
    final User user =
      restTemplate.postForObject(server + USERS_URL,
                                 createUserArgs,
                                 User.class);
    assertEquals(username, user.getUsername());

    final CreateRouteArgs createRouteArgs = CreateRouteArgs.builder().permissions(permissions).build();
    Route route = userRouteClient.createRoute(OneOfLongOrString.from(username),
                                              path,
                                              createRouteArgs);
    assertEquals(user.getId(), route.getUserIdentifier().getId());
    assertEquals(user.getUsername(), route.getUserIdentifier().getUsername());
    assertEquals(path, route.getPath());
    assertEquals(permissions, route.getPermissions());

    final URI uri =
      new URIBuilder()
        .setScheme("http")
        .setHost("localhost")
        .setPort(port)
        .setPath(ROUTES_URL)
        .addParameter("filter", "user.id==" + user.getId())
        .build();

    ResponseEntity<List<Route>> routesResponse =
      restTemplate.exchange(uri,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Route>>() {
                            });
    assertThat(routesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<Route> routes = routesResponse.getBody();
    assertEquals(1, routes.size());
    route = routes.get(0);
    assertEquals(user.getId(), route.getUserIdentifier().getId());
    assertEquals(user.getUsername(), route.getUserIdentifier().getUsername());
    assertEquals(path, route.getPath());
    assertEquals(permissions, route.getPermissions());

    userRouteClient.deleteRoute(OneOfLongOrString.from(username),
                                path,
                                null);

    routesResponse =
      restTemplate.exchange(uri,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<Route>>() {
                            });
    assertThat(routesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    routes = routesResponse.getBody();
    assertEquals(0, routes.size());
  }
}
