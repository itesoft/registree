package com.itesoft.registree.maven;

import java.nio.file.Path;
import java.util.Arrays;

import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;
import com.itesoft.registree.maven.test.MavenRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MavenProxyRegistryWithIncludeTest extends MavenRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-proxy";
  private static final String PROXY_NO_FILTERING_REGISTRY_NAME = "proxy-no-filtering";

  @BeforeAll
  public void setup() throws Exception {
    createProxyRegistry(PROXY_NO_FILTERING_REGISTRY_NAME,
                        "https://repo1.maven.org/maven2");
    final ProxyRegistryFiltering filtering =
      createProxyRegistryFiltering(Arrays.asList("jakarta/validation"),
                                   ProxyRegistryFilterPolicy.INCLUDE,
                                   ProxyRegistryFilterPolicy.EXCLUDE);
    createProxyRegistry("https://repo1.maven.org/maven2",
                        filtering);
    createAnonymousProxyReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void installArtifactWithIncludedDependency() throws Exception {
    Path settingsFile = getWithMirrorAnonymousSettingsFile(PROXY_NO_FILTERING_REGISTRY_NAME);
    mvnInstall(settingsFile,
               useExternalDependencyProjectFolder.toFile());

    removeMavenArtifactsFromLocalRepository("jakarta/validation");

    settingsFile = getWithMirrorAnonymousSettingsFile(PROXY_REGISTRY_NAME);
    mvnInstall(settingsFile,
               useExternalDependencyProjectFolder.toFile());
  }

  @Test
  public void installArtifactWithNotIncludedDependency() throws Exception {
    Path settingsFile = getWithMirrorAnonymousSettingsFile(PROXY_NO_FILTERING_REGISTRY_NAME);
    mvnInstall(settingsFile,
               useJakartaAnnotationProjectFolder.toFile());

    removeMavenArtifactsFromLocalRepository("jakarta/annotation");

    settingsFile = getWithMirrorAnonymousSettingsFile(PROXY_REGISTRY_NAME);
    mvnInstall(1,
               "Could not find artifact jakarta.annotation:jakarta.annotation-api",
               settingsFile,
               useJakartaAnnotationProjectFolder.toFile());
  }
}
