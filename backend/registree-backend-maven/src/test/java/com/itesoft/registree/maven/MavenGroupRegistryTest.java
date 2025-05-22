package com.itesoft.registree.maven;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.itesoft.registree.maven.test.MavenRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class MavenGroupRegistryTest extends MavenRegistryTest {
  private static final String HOSTED_REGISTRY_FOLDER_NAME = "registry-hosted";

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createProxyRegistry("https://repo1.maven.org/maven2");
    createGroupRegistry(Arrays.asList(HOSTED_REGISTRY_NAME, PROXY_REGISTRY_NAME));
    createAnonymousHostedReadWriteRoute();
    createAnonymousGroupReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { HOSTED_REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishThenInstallSingleArtifact() throws Exception {
    final Path hostedSettingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    final Path groupSettingsFile = getWithMirrorAnonymousSettingsFile(GROUP_REGISTRY_NAME);
    final Path firstProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_0_0Folder.toFile(),
                                    firstProjectPath.toFile());
    mvnDeploy(hostedSettingsFile,
              firstProjectPath.toFile());
    removeMavenArtifactsFromLocalRepository();
    mvnInstall(groupSettingsFile,
               useFirstProjectFolder.toFile());
  }
}
