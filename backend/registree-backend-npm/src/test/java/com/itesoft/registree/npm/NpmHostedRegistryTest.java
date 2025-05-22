package com.itesoft.registree.npm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.itesoft.registree.npm.dto.json.ResponsePackage;
import com.itesoft.registree.npm.dto.json.Version;
import com.itesoft.registree.npm.test.NpmRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NpmHostedRegistryTest extends NpmRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  private String npmrcPath;

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createAnonymousHostedReadWriteRoute();

    npmrcPath = createAnonymousNpmrc(HOSTED_REGISTRY_NAME);
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishAndInstallSingleLibrary() throws Exception {
    npmPublish(npmrcPath, rpc250303Library);
    cleanNpmStuff();
    npmView(npmrcPath, "@itesoft/rpc");
    npmView(npmrcPath, "@itesoft/rpc@25.3.3-master");
    curlSpecificVersion(HOSTED_REGISTRY_NAME, "@itesoft/rpc", "25.3.3-master", Version.class);
    npmInstall(npmrcPath, "@itesoft/rpc");
  }

  @Test
  public void publishAndInstallLibraryWithDependency() throws Exception {
    npmPublish(npmrcPath, exception250303Library);
    npmPublish(npmrcPath, rpc250303Library);
    cleanNpmStuff();
    npmInstall(npmrcPath, "@itesoft/exception");
  }

  @Test
  public void publishAndInstallSingleLibraryExactVersion() throws Exception {
    npmPublish(npmrcPath, rpc250303Library);
    npmPublish(npmrcPath, rpc250304Library);
    cleanNpmStuff();
    npmInstall(npmrcPath, "@itesoft/rpc@25.3.3-master");
  }

  @Test
  public void publishAndInstallUsingCacheVersion() throws Exception {
    npmPublish(npmrcPath, rpc250304Library);
    cleanNpmStuff();
    npmInstall(npmrcPath, "@itesoft/rpc");

    final Path tgzPathOnRegistry =
      Paths.get(registreeDataConfiguration.getRegistriesPath(),
                REGISTRY_FOLDER_NAME,
                "@itesoft/rpc/rpc-25.3.4-master.tgz");
    Files.delete(tgzPathOnRegistry);

    cleanPreviousNpmInstall();
    npmInstall(npmrcPath, "@itesoft/rpc");
  }

  @Test
  public void publishAndInstallSingleLibraryLowerVersion() throws Exception {
    npmPublish(npmrcPath, rpc250304Library);
    npmPublish(npmrcPath, rpc250303Library);
    final ResponsePackage responsePackage = curlPackageJson(HOSTED_REGISTRY_NAME, "@itesoft/rpc", ResponsePackage.class);
    final String latest = responsePackage.getDistTags().get("latest");
    assertEquals("25.3.4-master", latest);
  }
}
