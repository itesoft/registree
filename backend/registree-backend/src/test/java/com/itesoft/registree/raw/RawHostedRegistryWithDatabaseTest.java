package com.itesoft.registree.raw;

import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;
import static com.itesoft.registree.rest.test.RouteHelper.createAnonymousApiReadWriteDeleteRoute;

import jakarta.ws.rs.client.ClientBuilder;

import com.itesoft.registree.raw.test.RawRegistryTest;
import com.itesoft.registree.rest.test.ComponentClient;
import com.itesoft.registree.rest.test.FileClient;
import com.itesoft.registree.rest.test.RegistryResourceClient;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.sql.init.mode=always",
                                   "spring.sql.init.data-locations=classpath:/raw-hosted-test.sql",
                                   "spring.jpa.defer-datasource-initialization=false" })
public abstract class RawHostedRegistryWithDatabaseTest extends RawRegistryTest {
  protected static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @Value("classpath:docker-images/alpine.tar")
  protected Resource alpineTar;

  // FIXME: @LocalServerPort is not giving the right port
  private final int port = 8080;

  protected ComponentClient componentClient;
  protected FileClient fileClient;
  protected RegistryResourceClient registryResourceClient;

  @BeforeAll
  public void setup() throws Exception {
    final String server = "http://localhost:" + port;

    createAnonymousApiReadWriteDeleteRoute(server);

    final ResteasyClient client = (ResteasyClient) ClientBuilder.newClient();
    final ResteasyWebTarget target = client.target(server + API_URL_PREFIX);
    componentClient = target.proxy(ComponentClient.class);
    fileClient = target.proxy(FileClient.class);
    registryResourceClient = target.proxy(RegistryResourceClient.class);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }
}
