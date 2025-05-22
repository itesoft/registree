package com.itesoft.registree.maven;

import java.nio.file.Path;

import com.itesoft.registree.maven.test.MavenRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MavenProxyRegistryTest extends MavenRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  @BeforeAll
  public void setup() throws Exception {
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
  }
}
