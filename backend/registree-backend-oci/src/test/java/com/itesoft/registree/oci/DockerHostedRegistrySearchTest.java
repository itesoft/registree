package com.itesoft.registree.oci;

import static com.itesoft.registree.oci.TestHelper.getRepositories;
import static com.itesoft.registree.oci.TestHelper.getTags;
import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DockerHostedRegistrySearchTest extends DockerRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createAnonymousHostedReadWriteRoute();
  }

  @BeforeEach
  public void populateRegistry() throws Exception {
    populateRegistry("alpine");
    populateRegistry("alpine:3.21");
    populateRegistry("alpine:3");
    populateRegistry("alpine/curl");
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-hosted" };
  }

  @Override
  public String[] getDockerImagesToRemove() {
    return new String[] { "alpine", "alpine:3", "alpine:3.21" };
  }

  @Test
  public void listRepositories() throws Exception {
    final List<String> repositories = getRepositories(objectMapper, "http://localhost:8090");
    assertEquals(Arrays.asList("alpine", "alpine/curl"),
                 repositories);
  }

  @Test
  public void listTags() throws Exception {
    final List<String> tags = getTags(objectMapper, "http://localhost:8090", "alpine");
    assertEquals(Arrays.asList("3", "3.21", "latest"),
                 tags);
  }

  private void populateRegistry(final String nameAndTag) throws Exception {
    execute("docker", "pull", nameAndTag);
    execute("docker", "tag", nameAndTag, "localhost:8090/" + nameAndTag);
    execute("docker", "push", "localhost:8090/" + nameAndTag);
  }
}
