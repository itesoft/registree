package com.itesoft.registree.npm;

import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;
import static com.itesoft.registree.rest.test.RouteHelper.createAnonymousApiReadWriteDeleteRoute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import jakarta.ws.rs.client.ClientBuilder;

import com.itesoft.registree.dto.Component;
import com.itesoft.registree.dto.File;
import com.itesoft.registree.dto.Gav;
import com.itesoft.registree.npm.test.NpmRegistryTest;
import com.itesoft.registree.rest.test.ComponentClient;
import com.itesoft.registree.rest.test.FileClient;
import com.itesoft.registree.rest.test.RegistryResourceClient;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.jpa.defer-datasource-initialization=false" })
public abstract class NpmRegistryWithDatabaseTest extends NpmRegistryTest {
  // FIXME: @LocalServerPort is not giving the right port
  protected final int port = 8080;

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

  protected void assertComponentAndFiles(final String registryName,
                                         final String scope,
                                         final String name,
                                         final String version,
                                         final String username)
    throws Exception {
    final Gav gav = Gav.builder()
      .group(scope)
      .name(name)
      .version(version)
      .build();
    final Component component = componentClient.getComponent(registryName,
                                                             gav.toString());
    assertEquals(registryName, component.getRegistryName());
    assertEquals(scope, component.getGroup());
    assertEquals(name, component.getName());
    assertEquals(version, component.getVersion());

    final String filter = "component.id==" + component.getId();
    final List<File> files =
      fileClient.searchFiles(filter, null, null, null);
    assertEquals(1, files.size());

    final File file = files.get(0);
    assertEquals(registryName, file.getRegistryName());
    assertEquals(component.getId(), file.getComponentId());
    assertEquals("application/octet-stream", file.getContentType());
    if (scope != null) {
      assertEquals(scope + "/" + name + "/" + name + "-" + version + ".tgz", file.getPath());
    } else {
      assertEquals(name + "/" + name + "-" + version + ".tgz", file.getPath());
    }
    assertEquals(username, file.getUploader());
  }
}
