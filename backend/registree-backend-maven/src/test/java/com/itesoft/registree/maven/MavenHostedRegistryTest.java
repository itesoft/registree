package com.itesoft.registree.maven;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.itesoft.registree.maven.dto.xml.MavenMetadata;
import com.itesoft.registree.maven.test.MavenRegistryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

public class MavenHostedRegistryTest extends MavenRegistryTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @BeforeAll
  public void setup() throws Exception {
    createHostedRegistry();
    createAnonymousHostedReadWriteRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishThenInstallSingleArtifact() throws Exception {
    final Path settingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    final Path firstProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_0_0Folder.toFile(),
                                    firstProjectPath.toFile());
    mvnDeploy(settingsFile,
              firstProjectPath.toFile());
    removeMavenArtifactsFromLocalRepository();
    mvnInstall(settingsFile,
               useFirstProjectFolder.toFile());
  }

  @Test
  public void publishThenInstallArtifactWithDependency() throws Exception {
    final Path settingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    final Path firstProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_0_0Folder.toFile(),
                                    firstProjectPath.toFile());
    mvnDeploy(settingsFile,
              firstProjectPath.toFile());

    final Path secondProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(secondProject2_3_4Folder.toFile(),
                                    secondProjectPath.toFile());
    mvnDeploy(settingsFile,
              secondProjectPath.toFile());
    removeMavenArtifactsFromLocalRepository();
    mvnInstall(settingsFile,
               useSecondProjectFolder.toFile());
  }

  @Test
  public void publishMultipleVersionsOfArtifact() throws Exception {
    final Path settingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    final Path first1_0_0ProjectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(firstProject1_0_0Folder.toFile(),
                                    first1_0_0ProjectPath.toFile());
    mvnDeploy(settingsFile,
              first1_0_0ProjectPath.toFile());

    removeMavenArtifactsFromLocalRepository();

    mvnDeploy(firstProject1_1_0Folder, settingsFile);

    MavenMetadata metadata =
      curlMetadata(HOSTED_REGISTRY_NAME,
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

    removeMavenArtifactsFromLocalRepository();

    mvnDeploy(settingsFile,
              first1_0_0ProjectPath.toFile());

    metadata =
      curlMetadata(HOSTED_REGISTRY_NAME,
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
