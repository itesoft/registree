package com.itesoft.registree.npm;

import java.util.Arrays;

import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;
import com.itesoft.registree.npm.test.NpmRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmProxyRegistryWithIncludeFilterTest extends NpmRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  private String npmrcPath;

  @BeforeAll
  public void setup() throws Exception {
    final ProxyRegistryFiltering filtering =
      createProxyRegistryFiltering(Arrays.asList("debug", "ms"),
                                   ProxyRegistryFilterPolicy.INCLUDE,
                                   ProxyRegistryFilterPolicy.EXCLUDE);
    createProxyRegistry(filtering);
    createAnonymousProxyReadRoute();

    npmrcPath = createAnonymousNpmrc(PROXY_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void installIncludedLibrary() throws Exception {
    npmInstall(npmrcPath, "debug");
  }

  @Test
  public void installNotIncludedLibrary() throws Exception {
    npmInstall(1, "not found", npmrcPath, "type");
  }

  @Test
  public void installAnotherNotIncludedLibrary() throws Exception {
    npmInstall(1, "not found", npmrcPath, "@angular/cli");
  }
}
