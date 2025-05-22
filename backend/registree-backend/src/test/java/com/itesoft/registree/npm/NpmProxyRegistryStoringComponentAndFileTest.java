package com.itesoft.registree.npm;

import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmProxyRegistryStoringComponentAndFileTest extends NpmRegistryWithDatabaseTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  private String npmrcPath;

  @BeforeAll
  public void createRegistryAndRoute() throws Exception {
    createProxyRegistry();
    createAnonymousProxyReadRoute();

    npmrcPath = createAnonymousNpmrc(PROXY_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void installDifferentLibrariesWithSameName() throws Exception {
    npmInstall(npmrcPath, "expect@29.7.0");
    assertComponentAndFiles(PROXY_REGISTRY_NAME, null, "expect", "29.7.0", ANONYMOUS_USERNAME);

    cleanNpmStuff();

    npmInstall(npmrcPath, "@jest/expect@29.7.0");
    assertComponentAndFiles(PROXY_REGISTRY_NAME, "@jest", "expect", "29.7.0", ANONYMOUS_USERNAME);

    cleanNpmStuff();

    npmInstall(npmrcPath, "expect@29.7.0");
    assertComponentAndFiles(PROXY_REGISTRY_NAME, null, "expect", "29.7.0", ANONYMOUS_USERNAME);
  }

  @Test
  public void installSimpleLibrary() throws Exception {
    npmInstall(npmrcPath, "debug");
  }
}
