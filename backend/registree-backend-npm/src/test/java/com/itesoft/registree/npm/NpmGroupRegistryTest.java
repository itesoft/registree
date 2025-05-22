package com.itesoft.registree.npm;

import com.itesoft.registree.npm.test.NpmRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmGroupRegistryTest extends NpmRegistryTest {
  private String hostedNpmrcPath;
  private String groupNpmrcPath;

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createProxyRegistry(10);
    createGroupRegistry();

    createAnonymousHostedReadWriteRoute();
    createAnonymousGroupReadRoute();

    hostedNpmrcPath = createAnonymousNpmrc(HOSTED_REGISTRY_NAME);
    groupNpmrcPath = createAnonymousNpmrc(GROUP_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-hosted", "registry-proxy" };
  }

  @Test
  public void installFromGroup() throws Exception {
    npmPublish(hostedNpmrcPath, exception250303Library);
    npmPublish(hostedNpmrcPath, rpc250304Library);
    cleanNpmStuff();

    npmView(groupNpmrcPath, "debug");
    npmInstall(groupNpmrcPath, "debug");

    cleanNpmStuff();
    npmView(groupNpmrcPath, "@itesoft/exception");
    npmInstall(groupNpmrcPath, "@itesoft/exception");
  }
}
