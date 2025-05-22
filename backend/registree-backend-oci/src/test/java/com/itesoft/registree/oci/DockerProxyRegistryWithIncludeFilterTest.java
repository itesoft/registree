package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import java.util.Arrays;

import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;
import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DockerProxyRegistryWithIncludeFilterTest extends DockerRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    final ProxyRegistryFiltering filtering =
      createProxyRegistryFiltering(Arrays.asList("alpine/curl"),
                                   ProxyRegistryFilterPolicy.INCLUDE,
                                   ProxyRegistryFilterPolicy.EXCLUDE);
    createProxyRegistry(filtering);
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
  public void pullIncluded() throws Exception {
    execute("docker", "pull", "localhost:8070/alpine/curl");
    execute("docker", "rmi", "localhost:8070/alpine/curl");
  }

  @Test
  public void pullNotIncluded() throws Exception {
    execute(1, "unknown", 1, "docker", "pull", "localhost:8070/alpine");
  }
}
