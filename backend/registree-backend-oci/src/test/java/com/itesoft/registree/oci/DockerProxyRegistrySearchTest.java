package com.itesoft.registree.oci;

import static com.itesoft.registree.oci.TestHelper.getRepositories;
import static com.itesoft.registree.oci.TestHelper.getTags;
import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DockerProxyRegistrySearchTest extends DockerRegistryTest {
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
  public void listRepositories() throws Exception {
    execute("docker", "pull", "localhost:8070/alpine");
    execute("docker", "pull", "localhost:8070/alpine/curl");

    final List<String> repositories = getRepositories(objectMapper, "http://localhost:8070");
    assertNotNull(repositories);
    assertEquals(Arrays.asList("alpine", "alpine/curl"),
                 repositories);
  }

  @Test
  public void listTags() throws Exception {
    final List<String> tags = getTags(objectMapper,
                                      "http://localhost:8070",
                                      "alpine");
    assertNotNull(tags);
    assertThat(tags).hasSizeGreaterThan(100);
  }
}
