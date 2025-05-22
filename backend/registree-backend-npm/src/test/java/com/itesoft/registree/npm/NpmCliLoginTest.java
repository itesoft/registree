package com.itesoft.registree.npm;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import java.nio.file.Paths;

import com.itesoft.registree.npm.test.NpmRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmCliLoginTest extends NpmRegistryTest {
  private static final String USERNAME = "test";
  private static final String PASSWORD = "test";

  private String groupNpmrcPath;

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createProxyRegistry(10);
    createGroupRegistry();

    createUser(USERNAME, PASSWORD);
    createRoute(USERNAME,
                "/" + HOSTED_REGISTRY_NAME,
                "rw");
    createAnonymousGroupReadRoute();

    groupNpmrcPath = createAnonymousNpmrc(GROUP_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { "registry-hosted", "registry-proxy" };
  }

  @Test
  public void useNpmCliLogin() throws Exception {
    npmInstall(groupNpmrcPath, "npm-cli-login");

    final String registryUrl = String.format("http://localhost:%d/registry/%s/",
                                             port,
                                             HOSTED_REGISTRY_NAME);

    execute(Paths.get(NODE_MODULES_FOLDER_NAME, ".bin", "npm-cli-login").toAbsolutePath().toString(),
            "-r",
            registryUrl,
            "-u",
            USERNAME,
            "-p",
            PASSWORD,
            "-e",
            "test@test.test");
  }
}
