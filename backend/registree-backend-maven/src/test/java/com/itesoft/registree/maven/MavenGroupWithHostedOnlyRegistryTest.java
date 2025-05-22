package com.itesoft.registree.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.itesoft.registree.maven.dto.xml.MavenMetadata;
import com.itesoft.registree.maven.test.MavenRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class MavenGroupWithHostedOnlyRegistryTest extends MavenRegistryTest {
  private static final String HOSTED_REGISTRY_FOLDER_NAME = "registry-hosted";
  private static final String SECOND_HOSTED_REGISTRY_NAME = "second-hosted";
  private static final String SECOND_HOSTED_REGISTRY_FOLDER_NAME = "registry-second";

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createHostedRegistry(SECOND_HOSTED_REGISTRY_NAME, SECOND_HOSTED_REGISTRY_FOLDER_NAME);
    createGroupRegistry(Arrays.asList(HOSTED_REGISTRY_NAME,
                                      SECOND_HOSTED_REGISTRY_NAME));
    createAnonymousHostedReadWriteRoute();
    createAnonymousHostedReadWriteRoute(SECOND_HOSTED_REGISTRY_NAME);
    createAnonymousGroupReadRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { HOSTED_REGISTRY_FOLDER_NAME, SECOND_HOSTED_REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishThenInstallSingleArtifact() throws Exception {
    final Path hostedSettingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    final Path groupSettingsFile = getNoMirrorAnonymousSettingsFile(GROUP_REGISTRY_NAME);
    final Path firstProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_0_0Folder.toFile(),
                                    firstProjectPath.toFile());
    mvnDeploy(hostedSettingsFile,
              firstProjectPath.toFile());
    removeMavenArtifactsFromLocalRepository();
    mvnInstall(groupSettingsFile,
               useFirstProjectFolder.toFile());
  }

  @Test
  public void publishThenInstallArtifactWithDependencyOnDifferentRegistries() throws Exception {
    final Path firstHostedSettingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    final Path secondHostedSettingsFile = getNoMirrorAnonymousSettingsFile(SECOND_HOSTED_REGISTRY_NAME);
    final Path groupSettingsFile = getNoMirrorAnonymousSettingsFile(GROUP_REGISTRY_NAME);
    final Path firstProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_0_0Folder.toFile(),
                                    firstProjectPath.toFile());
    mvnDeploy(firstHostedSettingsFile,
              firstProjectPath.toFile(),
              HOSTED_REGISTRY_NAME);

    final Path secondProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(secondProject2_3_4Folder.toFile(),
                                    secondProjectPath.toFile());
    mvnDeploy(secondHostedSettingsFile,
              secondProjectPath.toFile(),
              SECOND_HOSTED_REGISTRY_NAME);
    mvnInstall(groupSettingsFile,
               useSecondProjectFolder.toFile());
  }

  @Test
  public void publishMultipleVersionsOfArtifactOnDifferentRegistries() throws Exception {
    final Path firstHostedSettingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    final Path secondHostedSettingsFile = getNoMirrorAnonymousSettingsFile(SECOND_HOSTED_REGISTRY_NAME);
    final Path first1_0_0ProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_0_0Folder.toFile(),
                                    first1_0_0ProjectPath.toFile());
    mvnDeploy(secondHostedSettingsFile,
              first1_0_0ProjectPath.toFile(),
              SECOND_HOSTED_REGISTRY_NAME);

    final Path first1_1_0ProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_1_0Folder.toFile(),
                                    first1_1_0ProjectPath.toFile());
    mvnDeploy(firstHostedSettingsFile,
              first1_1_0ProjectPath.toFile(),
              HOSTED_REGISTRY_NAME);

    MavenMetadata metadata =
      curlMetadata(GROUP_REGISTRY_NAME,
                   null,
                   null,
                   "com.itesoft.registree.test",
                   "registree-backend-maven-test-first-artifact",
                   MavenMetadata.class);
    assertEquals("1.1.0", metadata.getVersioning().getRelease());
    List<String> versions = metadata.getVersioning().getVersions();
    assertEquals(2, versions.size());
    assertEquals("1.0.0", versions.get(0));
    assertEquals("1.1.0", versions.get(1));

    mvnDeploy(secondHostedSettingsFile,
              first1_0_0ProjectPath.toFile(),
              SECOND_HOSTED_REGISTRY_NAME);

    metadata =
      curlMetadata(GROUP_REGISTRY_NAME,
                   null,
                   null,
                   "com.itesoft.registree.test",
                   "registree-backend-maven-test-first-artifact",
                   MavenMetadata.class);
    assertEquals("1.1.0", metadata.getVersioning().getRelease());
    versions = metadata.getVersioning().getVersions();
    assertEquals(2, versions.size());
    assertEquals("1.0.0", versions.get(0));
    assertEquals("1.1.0", versions.get(1));
  }
}
