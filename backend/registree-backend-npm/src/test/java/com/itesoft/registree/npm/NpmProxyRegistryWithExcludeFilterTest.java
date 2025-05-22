package com.itesoft.registree.npm;

import java.util.Arrays;

import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;
import com.itesoft.registree.npm.test.NpmRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmProxyRegistryWithExcludeFilterTest extends NpmRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  private String npmrcPath;

  @BeforeAll
  public void setup() throws Exception {
    final ProxyRegistryFiltering filtering =
      createProxyRegistryFiltering(Arrays.asList("debug"),
                                   ProxyRegistryFilterPolicy.EXCLUDE,
                                   ProxyRegistryFilterPolicy.INCLUDE);
    createProxyRegistry(filtering);
    createAnonymousProxyReadRoute();

    npmrcPath = createAnonymousNpmrc(PROXY_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void installExcludedLibrary() throws Exception {
    npmInstall(1, "not found", npmrcPath, "debug");
  }

  @Test
  public void installNotExcludedLibrary() throws Exception {
    npmInstall(npmrcPath, "type");
  }
}
