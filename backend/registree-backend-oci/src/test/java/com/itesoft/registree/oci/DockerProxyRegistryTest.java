package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DockerProxyRegistryTest extends DockerRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    createProxyRegistry();
    createAnonymousProxyReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-proxy" };
  }

  @Override
  public String[] getDockerImagesToRemove() {
    return new String[] { "alpine", "alpine/curl" };
  }

  @Test
  public void pullFromProxy() throws Exception {
    execute("docker", "pull", "localhost:8070/alpine");
    execute("docker", "pull", "localhost:8070/alpine/curl");

    execute("docker", "rmi", "localhost:8070/alpine");
    execute("docker", "rmi", "localhost:8070/alpine/curl");
  }

  @Test
  public void pushToProxy() throws Exception {
    execute("docker", "pull", "alpine");
    execute("docker", "tag", "alpine", "localhost:8070/alpine");
    execute(1,
            "unauthorized",
            1, // last line
            "docker",
            "push",
            "localhost:8070/alpine");
  }
}
