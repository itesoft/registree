package com.itesoft.registree.maven;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.itesoft.registree.maven.test.MavenRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class MavenHostedRegistryAuthenticationTest extends MavenRegistryTest {
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";
  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();

    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/" + HOSTED_REGISTRY_NAME,
                "rw");
    createAnonymousReadRoute(HOSTED_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishThenInstallSingleArtifact() throws Exception {
    final Path settingsFile = getNoMirrorAuthenticatedSettingsFile(HOSTED_REGISTRY_NAME, USERNAME, PASSWORD);
    final Path projectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(withAuthProject1_0_0Folder.toFile(),
                                    projectPath.toFile());
    final Path pomXmlPath = Paths.get(projectPath.toString(), "pom.xml");
    replaceFilePatterns(pomXmlPath.toFile(), HOSTED_REGISTRY_NAME, USERNAME, PASSWORD);
    mvnDeploy(settingsFile,
              projectPath.toFile());
    removeMavenArtifactsFromLocalRepository();
    mvnInstall(settingsFile,
               withAuthProject1_0_0Folder.toFile());
  }

  @Test
  public void publishWithAnonymousFails() throws Exception {
    final Path settingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    final Path firstProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_0_0Folder.toFile(),
                                    firstProjectPath.toFile());
    mvnDeployExpectUnauthorized(settingsFile,
                                firstProjectPath.toFile());
  }
}
