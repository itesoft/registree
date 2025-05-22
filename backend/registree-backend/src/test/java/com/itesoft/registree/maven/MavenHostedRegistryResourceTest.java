package com.itesoft.registree.maven;

import static com.itesoft.registree.maven.config.MavenConstants.METADATA_FILE_NAME;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import com.itesoft.registree.dto.Resource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MavenHostedRegistryResourceTest extends MavenHostedRegistryWithDatabaseTest {
  private static final String REGISTRY_FOLDER_NAME = "registry-hosted";

  @BeforeAll
  public void createRoute() throws Exception {
    createAnonymousHostedReadWriteRoute();
  }

  @Override
  public String[] getRegistryPaths() {
    return new String[] { REGISTRY_FOLDER_NAME };
  }

  @Test
  public void publishArtifactsCheckComponentAndFiles() throws Exception {
    final Path settingsFile = getNoMirrorAnonymousSettingsFile(HOSTED_REGISTRY_NAME);
    mvnDeploy(firstProject1_0_0Folder, settingsFile);
    mvnDeploy(firstProject1_1_0Folder, settingsFile);
    mvnDeploy(secondProject2_3_4Folder, settingsFile);

    final Consumer<Resource> directoryChecker = (resource) -> {
      assertEquals("directory", resource.getType());
      assertNull(resource.getRelativeDownloadPath());
      assertNull(resource.getComponentGav());
      assertNull(resource.getFilePath());
    };

    List<Resource> resources =
      registryResourceClient.getRootResources(HOSTED_REGISTRY_NAME);
    assertEquals(1, resources.size());
    Resource resource = resources.get(0);
    assertEquals("com", resource.getName());
    assertEquals("com", resource.getPath());
    assertEquals(null, resource.getParentPath());
    directoryChecker.accept(resource);

    getAndCheckFolder("com",
                      "itesoft");
    getAndCheckFolder("com/itesoft",
                      "registree");
    getAndCheckFolder("com/itesoft/registree",
                      "test");

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          "com/itesoft/registree/test");

    assertEquals(2, resources.size());
    checkFolder(resources.get(0),
                "com/itesoft/registree/test",
                FISRT_ARTIFACT_NAME);
    checkFolder(resources.get(1),
                "com/itesoft/registree/test",
                SECOND_ARTIFACT_NAME);

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME);
    assertEquals(5, resources.size());
    resource = resources.get(0);
    checkArtifactVersionFolder(resource,
                               "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME,
                               "1.0.0",
                               String.format("%s:%s:%s",
                                             "com.itesoft.registree.test",
                                             FISRT_ARTIFACT_NAME,
                                             "1.0.0"));

    resource = resources.get(1);
    checkArtifactVersionFolder(resource,
                               "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME,
                               "1.1.0",
                               String.format("%s:%s:%s",
                                             "com.itesoft.registree.test",
                                             FISRT_ARTIFACT_NAME,
                                             "1.1.0"));
    checkMetadataFile(resources.get(2),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME,
                      METADATA_FILE_NAME);
    checkMetadataFile(resources.get(3),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME,
                      METADATA_FILE_NAME + ".md5");
    checkMetadataFile(resources.get(4),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME,
                      METADATA_FILE_NAME + ".sha1");

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.0.0");
    assertEquals(6, resources.size());
    checkArtifactFile(resources.get(0),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.0.0",
                      FISRT_ARTIFACT_NAME + "-1.0.0.jar");
    checkArtifactFile(resources.get(1),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.0.0",
                      FISRT_ARTIFACT_NAME + "-1.0.0.jar.md5");
    checkArtifactFile(resources.get(2),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.0.0",
                      FISRT_ARTIFACT_NAME + "-1.0.0.jar.sha1");
    checkArtifactFile(resources.get(3),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.0.0",
                      FISRT_ARTIFACT_NAME + "-1.0.0.pom");
    checkArtifactFile(resources.get(4),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.0.0",
                      FISRT_ARTIFACT_NAME + "-1.0.0.pom.md5");
    checkArtifactFile(resources.get(5),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.0.0",
                      FISRT_ARTIFACT_NAME + "-1.0.0.pom.sha1");

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.1.0");
    assertEquals(6, resources.size());
    checkArtifactFile(resources.get(0),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.1.0",
                      FISRT_ARTIFACT_NAME + "-1.1.0.jar");
    checkArtifactFile(resources.get(1),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.1.0",
                      FISRT_ARTIFACT_NAME + "-1.1.0.jar.md5");
    checkArtifactFile(resources.get(2),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.1.0",
                      FISRT_ARTIFACT_NAME + "-1.1.0.jar.sha1");
    checkArtifactFile(resources.get(3),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.1.0",
                      FISRT_ARTIFACT_NAME + "-1.1.0.pom");
    checkArtifactFile(resources.get(4),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.1.0",
                      FISRT_ARTIFACT_NAME + "-1.1.0.pom.md5");
    checkArtifactFile(resources.get(5),
                      "com/itesoft/registree/test/" + FISRT_ARTIFACT_NAME + "/1.1.0",
                      FISRT_ARTIFACT_NAME + "-1.1.0.pom.sha1");

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME);
    assertEquals(4, resources.size());
    resource = resources.get(0);
    checkArtifactVersionFolder(resource,
                               "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME,
                               "2.3.4",
                               String.format("%s:%s:%s",
                                             "com.itesoft.registree.test",
                                             SECOND_ARTIFACT_NAME,
                                             "2.3.4"));
    checkMetadataFile(resources.get(1),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME,
                      METADATA_FILE_NAME);
    checkMetadataFile(resources.get(2),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME,
                      METADATA_FILE_NAME + ".md5");
    checkMetadataFile(resources.get(3),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME,
                      METADATA_FILE_NAME + ".sha1");

    resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME + "/2.3.4");
    assertEquals(6, resources.size());
    checkArtifactFile(resources.get(0),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME + "/2.3.4",
                      SECOND_ARTIFACT_NAME + "-2.3.4.jar");
    checkArtifactFile(resources.get(1),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME + "/2.3.4",
                      SECOND_ARTIFACT_NAME + "-2.3.4.jar.md5");
    checkArtifactFile(resources.get(2),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME + "/2.3.4",
                      SECOND_ARTIFACT_NAME + "-2.3.4.jar.sha1");
    checkArtifactFile(resources.get(3),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME + "/2.3.4",
                      SECOND_ARTIFACT_NAME + "-2.3.4.pom");
    checkArtifactFile(resources.get(4),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME + "/2.3.4",
                      SECOND_ARTIFACT_NAME + "-2.3.4.pom.md5");
    checkArtifactFile(resources.get(5),
                      "com/itesoft/registree/test/" + SECOND_ARTIFACT_NAME + "/2.3.4",
                      SECOND_ARTIFACT_NAME + "-2.3.4.pom.sha1");
  }

  private void getAndCheckFolder(final String parentPath,
                                 final String name) {
    final List<Resource> resources =
      registryResourceClient.getResources(HOSTED_REGISTRY_NAME,
                                          parentPath);
    assertEquals(1, resources.size());
    checkFolder(resources.get(0), parentPath, name);
  }

  private void checkFolder(final Resource resource,
                           final String parentPath,
                           final String name) {
    assertEquals(name, resource.getName());
    assertEquals(parentPath + "/" + name, resource.getPath());
    assertEquals(parentPath, resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());
  }

  private void checkArtifactVersionFolder(final Resource resource,
                                          final String parentPath,
                                          final String name,
                                          final String gav) {
    assertEquals(name, resource.getName());
    assertEquals(parentPath + "/" + name, resource.getPath());
    assertEquals(parentPath, resource.getParentPath());
    assertEquals("directory", resource.getType());
    assertNull(resource.getRelativeDownloadPath());
    assertEquals(gav,
                 resource.getComponentGav());
    assertNull(resource.getFilePath());
  }

  private void checkMetadataFile(final Resource resource,
                                 final String parentPath,
                                 final String name) {
    assertEquals(name, resource.getName());
    assertEquals(parentPath + "/" + name, resource.getPath());
    assertEquals(parentPath, resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals(parentPath + "/" + name, resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertNull(resource.getFilePath());
  }

  private void checkArtifactFile(final Resource resource,
                                 final String parentPath,
                                 final String name) {
    assertEquals(name, resource.getName());
    assertEquals(parentPath + "/" + name, resource.getPath());
    assertEquals(parentPath, resource.getParentPath());
    assertEquals("file", resource.getType());
    assertEquals(parentPath + "/" + name, resource.getRelativeDownloadPath());
    assertNull(resource.getComponentGav());
    assertEquals(parentPath + "/" + name, resource.getFilePath());
  }
}
