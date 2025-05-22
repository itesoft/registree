package com.itesoft.registree.oci;

import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;
import static com.itesoft.registree.rest.test.RouteHelper.createAnonymousApiReadWriteDeleteRoute;
import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import jakarta.ws.rs.client.ClientBuilder;

import com.itesoft.registree.oci.test.DockerRegistryTest;
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
                                   "spring.sql.init.data-locations=classpath:/docker-hosted-test.sql",
                                   "spring.jpa.defer-datasource-initialization=false" })
public abstract class DockerHostedRegistryWithDatabaseTest extends DockerRegistryTest {
  protected static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  protected static final String ALPINE_MANIFEST_SHA = "sha256:ec1b05d1eac264d9204a57f4ad9d4dc35e9e756e9fedaea0674aefc7edb1d6a4";
  protected static final String ALPINE_CONFIG_SHA = "sha256:aded1e1a5b3705116fa0a92ba074a5e0b0031647d9c315983ccba2ee5428ec8b";
  protected static final String ALPINE_ALPINE_CURL_SHARED_LAYER_SHA = "sha256:f18232174bc91741fdf3da96d85011092101a032a93a388b79e99e69c2d5c870";
  protected static final String ALPINE_CURL_MANIFEST_SHA = "sha256:c2b98c82f68715ba3d5954567abedbf5ad2bf3c17a213f39982b6e819dc7f3e9";
  protected static final String ALPINE_CURL_CONFIG_SHA = "sha256:45979415db54959d4fd0033b3c6e19ef899fe30bc895920f0602a0eae4c973a2";
  protected static final String[] ALPINE_CURL_LAYERS_SHA = { "sha256:27e6e40720fbeb38b7a37a31b6c88a4ed775105d8867ae0cf78b2ec8a3125ccb",
                                                             "sha256:c74ef9212092cd7dd39cb832a9b7ba82a41991352c5f7a6ae31c4dfdd73c1ea3" };
  protected static final String[] BLOBS = { ALPINE_MANIFEST_SHA,
                                            ALPINE_CONFIG_SHA,
                                            ALPINE_ALPINE_CURL_SHARED_LAYER_SHA,
                                            ALPINE_CURL_MANIFEST_SHA,
                                            ALPINE_CURL_CONFIG_SHA,
                                            ALPINE_CURL_LAYERS_SHA[0],
                                            ALPINE_CURL_LAYERS_SHA[1] };

  @Value("classpath:docker-images/alpine.tar")
  protected Resource alpineTar;

  @Value("classpath:docker-images/alpine-curl.tar")
  protected Resource alpineCurlTar;

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

  protected void initWithEmbedImages() throws Exception {
    execute("sh", "-c", "docker load < " + alpineTar.getFile());
    execute("sh", "-c", "docker load < " + alpineCurlTar.getFile());

    execute("docker", "tag", "alpine", "localhost:8090/alpine");
    execute("docker", "tag", "alpine", "localhost:8090/alpine:ehe");
    execute("docker", "tag", "alpine", "localhost:8090/alpine:test");
    execute("docker", "tag", "alpine/curl", "localhost:8090/alpine/curl");
    execute("docker", "tag", "alpine/curl", "localhost:8090/alpine/curl:ehe");

    execute("docker", "push", "localhost:8090/alpine");
    execute("docker", "push", "localhost:8090/alpine:ehe");
    execute("docker", "push", "localhost:8090/alpine:test");
    execute("docker", "push", "localhost:8090/alpine/curl");
    execute("docker", "push", "localhost:8090/alpine/curl:ehe");

    execute("docker", "rmi", "alpine");
    execute("docker", "rmi", "alpine/curl");

    execute("docker", "rmi", "localhost:8090/alpine");
    execute("docker", "rmi", "localhost:8090/alpine:ehe");
    execute("docker", "rmi", "localhost:8090/alpine:test");
    execute("docker", "rmi", "localhost:8090/alpine/curl");
    execute("docker", "rmi", "localhost:8090/alpine/curl:ehe");

    execute("docker", "pull", "localhost:8090/alpine");
    execute("docker", "pull", "localhost:8090/alpine:ehe");
    execute("docker", "pull", "localhost:8090/alpine:test");
    execute("docker", "pull", "localhost:8090/alpine/curl");
    execute("docker", "pull", "localhost:8090/alpine/curl:ehe");

    execute("docker", "rmi", "localhost:8090/alpine");
    execute("docker", "rmi", "localhost:8090/alpine:ehe");
    execute("docker", "rmi", "localhost:8090/alpine:test");
    execute("docker", "rmi", "localhost:8090/alpine/curl");
    execute("docker", "rmi", "localhost:8090/alpine/curl:ehe");
  }
}
