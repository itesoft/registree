package com.itesoft.registree.rest.test;

import static com.itesoft.registree.persistence.WellKnownUsers.ADMIN_USERNAME;
import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;
import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;

import jakarta.ws.rs.client.ClientBuilder;

import com.itesoft.registree.dto.CreateRouteArgs;
import com.itesoft.registree.dto.OneOfLongOrString;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public abstract class RouteHelper {
  public static void createAnonymousApiReadWriteDeleteRoute(final String serverUri) {
    final ResteasyClient adminClient = (ResteasyClient) ClientBuilder.newClient();
    adminClient.register(new AddAuthHeadersRequestFilter(ADMIN_USERNAME, "admin"));
    final ResteasyWebTarget adminTarget = adminClient.target(serverUri + API_URL_PREFIX);
    final UserRouteClient userRouteClient = adminTarget.proxy(UserRouteClient.class);

    userRouteClient.createRoute(OneOfLongOrString.from(ANONYMOUS_USERNAME),
                                "/api/v1",
                                CreateRouteArgs.builder()
                                  .permissions("rwd")
                                  .build());
  }

  private RouteHelper() {
  }
}
