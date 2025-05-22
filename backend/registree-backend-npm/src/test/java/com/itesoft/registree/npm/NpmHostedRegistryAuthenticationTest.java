package com.itesoft.registree.npm;

import com.itesoft.registree.npm.dto.json.Version;
import com.itesoft.registree.npm.test.NpmRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmHostedRegistryAuthenticationTest extends NpmRegistryTest {
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  private String npmrcPath;
  private String anonymousNpmrcPath;

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();

    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/" + HOSTED_REGISTRY_NAME,
                "rw");

    npmrcPath = createNpmrc(HOSTED_REGISTRY_NAME, USERNAME, PASSWORD);
    anonymousNpmrcPath = createAnonymousNpmrc(HOSTED_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-hosted" };
  }

  @Test
  public void publishAndInstallLibraryWithDependency() throws Exception {
    npmPublish(npmrcPath, exception250303Library);
    npmPublish(npmrcPath, rpc250303Library);
    curlSpecificVersion(HOSTED_REGISTRY_NAME, USERNAME, PASSWORD, "@itesoft/rpc", "25.3.3-master", Version.class);
    cleanNpmStuff();
    npmInstall(npmrcPath, "@itesoft/exception");
  }

  @Test
  public void publishAsAnonymousFails() throws Exception {
    npmPublish(1, "E401", anonymousNpmrcPath, exception250303Library);
  }
}
