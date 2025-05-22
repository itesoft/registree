package com.itesoft.registree.maven;

import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MavenProxyRegistryStoringComponentAndFileTest extends MavenRegistryWithDatabaseTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  @BeforeAll
  public void init() throws Exception {
    createProxyRegistry("https://repo1.maven.org/maven2");
    createAnonymousProxyReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void installArtifactWithExternalDependency() throws Exception {
    removeMavenArtifactsFromLocalRepository("jakarta/validation");

    final Path settingsFile = getWithMirrorAnonymousSettingsFile(PROXY_REGISTRY_NAME);
    mvnInstall(settingsFile,
               useExternalDependencyProjectFolder.toFile());

    assertComponentAndFiles(PROXY_REGISTRY_NAME,
                            "jakarta.validation",
                            "jakarta.validation-api",
                            "3.1.1",
                            ANONYMOUS_USERNAME);
  }
}
