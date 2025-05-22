package com.itesoft.registree.maven;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MavenHostedRegistryStoringComponentAndFileTest extends MavenHostedRegistryWithDatabaseTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  @BeforeAll
  public void createUserAndRoute() throws Exception {
    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/hosted",
                "rw");

    createAnonymousReadRoute(HOSTED_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishArtifactsCheckComponentAndFiles() throws Exception {
    final Path settingsFile = getNoMirrorAuthenticatedSettingsFile(HOSTED_REGISTRY_NAME, USERNAME, PASSWORD);

    mvnDeploy(firstProject1_0_0Folder, settingsFile);
    assertComponentAndFiles("com.itesoft.registree.test",
                            "registree-backend-maven-test-first-artifact",
                            "1.0.0",
                            USERNAME);

    mvnDeploy(secondProject2_3_4Folder, settingsFile);
    assertComponentAndFiles("com.itesoft.registree.test",
                            "registree-backend-maven-test-second-artifact",
                            "2.3.4",
                            USERNAME);
  }
}
