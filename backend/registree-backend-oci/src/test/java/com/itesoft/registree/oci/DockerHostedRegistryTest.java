package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DockerHostedRegistryTest extends DockerRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createAnonymousHostedReadWriteRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-hosted" };
  }

  @Override
  public String[] getDockerImagesToRemove() {
    return new String[] { "alpine", "alpine/curl" };
  }

  @Test
  public void nativeDockerPushAndPull() throws Exception {
    execute("docker", "pull", "alpine");
    execute("docker", "tag", "alpine", "localhost:8090/alpine");
    execute("docker", "push", "localhost:8090/alpine");
    execute("docker", "rmi", "alpine");
    execute("docker", "rmi", "localhost:8090/alpine");
    execute("docker", "pull", "localhost:8090/alpine");
    execute("docker", "rmi", "localhost:8090/alpine");
  }

  @Test
  public void nativeDockerPushAndPullWithComplexName() throws Exception {
    execute("docker", "pull", "alpine/curl");
    execute("docker", "tag", "alpine/curl", "localhost:8090/alpine/curl");
    execute("docker", "push", "localhost:8090/alpine/curl");
    execute("docker", "rmi", "alpine/curl");
    execute("docker", "rmi", "localhost:8090/alpine/curl");
    execute("docker", "pull", "localhost:8090/alpine/curl");
    execute("docker", "rmi", "localhost:8090/alpine/curl");
  }
}
