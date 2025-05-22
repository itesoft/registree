package com.itesoft.registree.oci.test;

import static com.itesoft.registree.oci.test.DockerHelper.removeDockerImages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.itesoft.registree.dto.ProxyRegistryFiltering;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;

public abstract class DockerRegistryTest extends OciRegistryTest {
  @BeforeEach
  public void dockerCleanup() throws IOException, InterruptedException {
    for (final String imageName : getDockerImagesToRemove()) {
      removeDockerImages(imageName);
    }
  }

  protected abstract String[] getDockerImagesToRemove();

  protected void createProxyRegistry()
    throws JsonProcessingException {
    createProxyRegistry(true, null);
  }

  protected void createProxyRegistry(final boolean doStore)
    throws JsonProcessingException {
    createProxyRegistry(doStore, null);
  }

  protected void createProxyRegistry(final ProxyRegistryFiltering filtering)
    throws JsonProcessingException {
    createProxyRegistry(true, filtering);
  }

  protected void createProxyRegistry(final boolean doStore,
                                     final ProxyRegistryFiltering filtering)
    throws JsonProcessingException {
    final Map<String, Object> additionalConfiguration = new HashMap<>();
    additionalConfiguration.put("port", 8070);
    createProxyRegistry(doStore,
                        "https://registry-1.docker.io",
                        0,
                        filtering,
                        additionalConfiguration);
  }
}
