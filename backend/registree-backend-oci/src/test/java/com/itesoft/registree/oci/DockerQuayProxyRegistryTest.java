package com.itesoft.registree.oci;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.oci.test.DockerRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DockerQuayProxyRegistryTest extends DockerRegistryTest {
  @BeforeAll
  public void setup() throws Exception {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("port", 8070);
    configurationAsMap.put("storagePath", "registry-proxy");
    configurationAsMap.put("doStore", true);
    configurationAsMap.put("proxyUrl", "https://quay.io");
    createProxyRegistry(configurationAsMap);
    createAnonymousProxyReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-proxy" };
  }

  @Override
  public String[] getDockerImagesToRemove() {
    return new String[] { "cilium/alpine-curl", "jitesoft/alpine" };
  }

  @Test
  public void pullFromProxy() throws Exception {
    execute("docker", "pull", "localhost:8070/cilium/alpine-curl");
    execute("docker", "pull", "localhost:8070/jitesoft/alpine");

    execute("docker", "rmi", "localhost:8070/cilium/alpine-curl");
    execute("docker", "rmi", "localhost:8070/jitesoft/alpine");
  }
}
