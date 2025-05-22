package com.itesoft.registree.npm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmHostedRegistryStoringComponentAndFileTest extends NpmHostedRegistryWithDatabaseTest {
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  private String npmrcPath;

  @BeforeAll
  public void createUserAndRoute() throws Exception {
    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/hosted",
                "rw");

    npmrcPath = createNpmrc(HOSTED_REGISTRY_NAME, USERNAME, PASSWORD);
  }

  @Test
  public void publishAndInstallSingleLibrary() throws Exception {
    npmPublish(npmrcPath, rpc250303Library);

    assertComponentAndFiles("@itesoft", "rpc", "25.3.3-master", USERNAME);

    npmPublish(npmrcPath, rpc250304Library);

    assertComponentAndFiles("@itesoft", "rpc", "25.3.4-master", USERNAME);
  }
}
