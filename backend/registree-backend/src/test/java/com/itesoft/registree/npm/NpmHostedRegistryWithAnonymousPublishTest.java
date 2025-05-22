package com.itesoft.registree.npm;

import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmHostedRegistryWithAnonymousPublishTest extends NpmHostedRegistryWithDatabaseTest {
  private String npmrcPath;

  @BeforeAll
  public void createRoute() throws Exception {
    createAnonymousHostedReadWriteRoute();

    npmrcPath = createAnonymousNpmrc(HOSTED_REGISTRY_NAME);
  }

  @Test
  public void publishAndInstallSingleLibrary() throws Exception {
    npmPublish(npmrcPath, rpc250303Library);

    assertComponentAndFiles("@itesoft", "rpc", "25.3.3-master", ANONYMOUS_USERNAME);
  }
}
