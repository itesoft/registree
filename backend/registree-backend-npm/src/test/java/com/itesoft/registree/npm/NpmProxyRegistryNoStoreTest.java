package com.itesoft.registree.npm;

import com.itesoft.registree.npm.dto.json.Version;
import com.itesoft.registree.npm.test.NpmRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmProxyRegistryNoStoreTest extends NpmRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  private String npmrcPath;

  @BeforeAll
  public void setup() throws Exception {
    createProxyRegistry(false, 10);
    createAnonymousProxyReadRoute();

    npmrcPath = createAnonymousNpmrc(PROXY_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void installSimpleLibrary() throws Exception {
    npmView(npmrcPath, "debug");
    npmView(npmrcPath, "debug@4.4.0");
    curlSpecificVersion(PROXY_REGISTRY_NAME, "debug", "4.4.0", Version.class);
    npmInstall(npmrcPath, "debug");
    cleanPreviousNpmInstall();
    npmInstall(npmrcPath, "debug");
  }
}
