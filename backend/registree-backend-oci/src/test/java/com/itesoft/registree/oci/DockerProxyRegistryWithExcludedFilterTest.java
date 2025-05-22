package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import java.util.Arrays;

import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;
import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DockerProxyRegistryWithExcludedFilterTest extends DockerRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    final ProxyRegistryFiltering filtering =
      createProxyRegistryFiltering(Arrays.asList("alpine/curl"),
                                   ProxyRegistryFilterPolicy.EXCLUDE,
                                   ProxyRegistryFilterPolicy.INCLUDE);
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
  public void pullExcluded() throws Exception {
    execute(1, "unknown", 1, "docker", "pull", "localhost:8070/alpine/curl");
  }

  @Test
  public void pullNotExcluded() throws Exception {
    execute("docker", "pull", "localhost:8070/alpine");
    execute("docker", "rmi", "localhost:8070/alpine");
  }
}
