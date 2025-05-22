package com.itesoft.registree.rest;

import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;
import static com.itesoft.registree.rest.test.RouteHelper.createAnonymousApiReadWriteDeleteRoute;

import jakarta.ws.rs.client.ClientBuilder;

import com.itesoft.registree.rest.test.ComponentClient;
import com.itesoft.registree.rest.test.FileClient;
import com.itesoft.registree.rest.test.RegistryClient;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RestControllerTest {
  protected static final String TOKENS_URL = API_URL_PREFIX + "/tokens";

  @LocalServerPort
  private int port;

  protected String server;
  protected RegistryClient registryClient;
  protected ComponentClient componentClient;
  protected FileClient fileClient;

  @BeforeAll
  public void init() {
    server = "http://localhost:" + port;

    final ResteasyClient client = (ResteasyClient) ClientBuilder.newClient();
    final ResteasyWebTarget target = client.target(server + API_URL_PREFIX);
    registryClient = target.proxy(RegistryClient.class);
    componentClient = target.proxy(ComponentClient.class);
    fileClient = target.proxy(FileClient.class);

    createAnonymousApiReadWriteDeleteRoute(server);
  }
}
