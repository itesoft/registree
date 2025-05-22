package com.itesoft.registree.maven;

import java.nio.file.Path;
import java.util.Arrays;

import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;
import com.itesoft.registree.maven.test.MavenRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MavenProxyRegistryWithExcludeTest extends MavenRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";

  @BeforeAll
  public void setup() throws Exception {
    final ProxyRegistryFiltering filtering =
      createProxyRegistryFiltering(Arrays.asList("jakarta/validation/jakarta.validation-api"),
                                   ProxyRegistryFilterPolicy.EXCLUDE,
                                   ProxyRegistryFilterPolicy.INCLUDE);
    createProxyRegistry("https://repo1.maven.org/maven2",
                        filtering);
    createAnonymousProxyReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void installArtifactWithExcludedDependency() throws Exception {
    removeMavenArtifactsFromLocalRepository("jakarta/validation");

    final Path settingsFile = getWithMirrorAnonymousSettingsFile(PROXY_REGISTRY_NAME);
    mvnInstall(1,
               "Could not find artifact jakarta.validation:jakarta.validation-api",
               settingsFile,
               useExternalDependencyProjectFolder.toFile());
  }

  @Test
  public void installArtifactWithNotExcludedDependency() throws Exception {
    removeMavenArtifactsFromLocalRepository("jakarta/annotation");

    final Path settingsFile = getWithMirrorAnonymousSettingsFile(PROXY_REGISTRY_NAME);
    mvnInstall(settingsFile,
               useJakartaAnnotationProjectFolder.toFile());
  }
}
