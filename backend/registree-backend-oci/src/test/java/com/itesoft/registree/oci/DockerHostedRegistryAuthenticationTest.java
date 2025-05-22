package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DockerHostedRegistryAuthenticationTest extends DockerRegistryTest {
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();

    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/hosted",
                "rw");

    execute("docker", "login", "-u", USERNAME, "-p", PASSWORD, "localhost:8090");
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-hosted" };
  }

  @Override
  public String[] getDockerImagesToRemove() {
    return new String[] { "alpine" };
  }

  @Test
  public void nativeDockerPushAndPull() throws Exception {
    execute("docker", "pull", "alpine");
    execute("docker", "tag", "alpine", "localhost:8090/alpine");
    execute("docker", "push", "localhost:8090/alpine");
  }

  @Test
  public void dockerLoginWrongAuth() throws Exception {
    execute(1,
            "unauthorized",
            1, // last line
            "docker",
            "login",
            "-u",
            "admin",
            "-p",
            "wrong",
            "localhost:8090");
  }
}
