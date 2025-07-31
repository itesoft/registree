package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "spring.sql.init.mode=always",
                                   "spring.sql.init.data-locations=classpath:/docker-group-test.sql",
                                   "spring.jpa.defer-datasource-initialization=false" })
public class DockerGroupRegistryDatabaseConfigTest extends DockerRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    createAnonymousHostedReadWriteRoute();
    createAnonymousGroupReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-group" };
  }

  @Override
  public String[] getDockerImagesToRemove() {
    return new String[] { "alpine", "alpine/curl" };
  }

  @Test
  public void pullFromGroup() throws Exception {
    execute("docker", "pull", "alpine");
    execute("docker", "tag", "alpine", registreeHostname + ":8090/alpine:ehe");
    execute("docker", "push", registreeHostname + ":8090/alpine:ehe");

    execute("docker", "pull", registreeHostname + ":8060/alpine");
    execute("docker", "pull", registreeHostname + ":8060/alpine:ehe");
    execute("docker", "pull", registreeHostname + ":8060/alpine/curl");

    execute("docker", "rmi", "alpine");
    execute("docker", "rmi", registreeHostname + ":8060/alpine");
    execute("docker", "rmi", registreeHostname + ":8060/alpine:ehe");
    execute("docker", "rmi", registreeHostname + ":8060/alpine/curl");
  }
}
