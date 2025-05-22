package com.itesoft.registree.maven.test;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itesoft.registree.configuration.RegistreeXmlConfiguration;
import com.itesoft.registree.spring.test.RegistryTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileSystemUtils;

public abstract class MavenRegistryTest extends RegistryTest {
  protected static final String PROJECT_FOLDER_PREFIX = "maven-project";
  protected static final String FISRT_ARTIFACT_NAME = "registree-backend-maven-test-first-artifact";
  protected static final String SECOND_ARTIFACT_NAME = "registree-backend-maven-test-second-artifact";
  private static final String SETTINGS_FILE_PREFIX = "settings";

  // CHECKSTYLE:OFF
  protected final Path firstProject1_0_0Folder = Paths.get(RESOURCES_FOLDER, "maven/projects", FISRT_ARTIFACT_NAME, "1.0.0");
  protected final Path firstProject1_1_0Folder = Paths.get(RESOURCES_FOLDER, "maven/projects", FISRT_ARTIFACT_NAME, "1.1.0");
  protected final Path secondProject2_3_4Folder = Paths.get(RESOURCES_FOLDER, "maven/projects", SECOND_ARTIFACT_NAME, "2.3.4");
  protected final Path withAuthProject1_0_0Folder =
    Paths.get(RESOURCES_FOLDER, "maven/projects/registree-backend-maven-test-with-auth/1.0.0");
  protected final Path useFirstProjectFolder =
    Paths.get(RESOURCES_FOLDER, "maven/projects/registree-backend-maven-test-use-first-artifact/1.0.0");
  protected final Path useSecondProjectFolder =
    Paths.get(RESOURCES_FOLDER, "maven/projects/registree-backend-maven-test-use-second-artifact/2.3.4");
  protected final Path useExternalDependencyProjectFolder =
    Paths.get(RESOURCES_FOLDER, "maven/projects/registree-backend-maven-test-use-external-dependency/0.0.1");
  protected final Path useJakartaAnnotationProjectFolder =
    Paths.get(RESOURCES_FOLDER, "maven/projects/registree-backend-maven-test-use-jakarta-annotation/0.0.1");
  // CHECKSTYLE:ON

  @Autowired
  private RegistreeXmlConfiguration registreeXmlConfiguration;

  private final Path noMirrorSettingsXmlFile = Paths.get(RESOURCES_FOLDER, "maven/no-mirror-settings.xml");
  private final Path withMirrorSettingsXmlFile = Paths.get(RESOURCES_FOLDER, "/maven/with-mirror-settings.xml");

  @BeforeAll
  public static synchronized void prepareResources() throws IOException {
    prepareResources(MavenRegistryTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
  }

  @Override
  protected String getFormat() {
    return "maven";
  }

  @BeforeEach
  protected void removeMavenProject() throws Exception {
    Files.walk(Paths.get("."))
      .filter(p -> {
        final String fileName = p.getFileName().toString();
        return fileName.startsWith(PROJECT_FOLDER_PREFIX)
          || fileName.startsWith(SETTINGS_FILE_PREFIX);
      })
      .forEach(p -> {
        try {
          FileSystemUtils.deleteRecursively(p);
        } catch (final IOException exception) {
          fail(exception.getMessage());
        }
      });
  }

  @BeforeEach
  protected void removeMavenArtifactsFromLocalRepository() throws Exception {
    removeMavenArtifactsFromLocalRepository("com/itesoft/registree/test");
  }

  protected void removeMavenArtifactsFromLocalRepository(final String path) throws Exception {
    final String userHome = System.getProperty("user.home");
    final Path registreeTestArtifactsFolder = Path.of(userHome,
                                                      ".m2/repository/" + path);
    FileSystemUtils.deleteRecursively(registreeTestArtifactsFolder);
  }

  protected void createHostedRegistry() throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("storagePath", "registry-hosted");
    createHostedRegistry(configurationAsMap);
  }

  protected void createHostedRegistry(final String registryName,
                                      final String storagePath)
    throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("storagePath", storagePath);
    createHostedRegistry(registryName, configurationAsMap);
  }

  protected void createGroupRegistry(final List<String> memberNames)
    throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("memberNames", memberNames);
    createGroupRegistry(configurationAsMap);
  }

  protected Path getNoMirrorAnonymousSettingsFile(final String registryName) throws IOException {
    return getSettingsFile(noMirrorSettingsXmlFile,
                           registryName,
                           "",
                           "");
  }

  protected Path getNoMirrorAuthenticatedSettingsFile(final String registryName,
                                                      final String username,
                                                      final String password)
    throws IOException {
    return getSettingsFile(noMirrorSettingsXmlFile,
                           registryName,
                           username,
                           password);
  }

  protected Path getWithMirrorAnonymousSettingsFile(final String registryName) throws IOException {
    return getSettingsFile(withMirrorSettingsXmlFile,
                           registryName,
                           "",
                           "");
  }

  protected void mvnDeploy(final Path projectFolder,
                           final Path settingsFile)
    throws Exception {
    final Path projectPath = Files.createTempDirectory(Paths.get("."), PROJECT_FOLDER_PREFIX);
    FileSystemUtils.copyRecursively(projectFolder.toFile(),
                                    projectPath.toFile());
    mvnDeploy(settingsFile,
              projectPath.toFile());
  }

  protected void mvnDeploy(final Path settingsXml,
                           final File projectPath)
    throws Exception {
    mvnDeploy(settingsXml, projectPath, HOSTED_REGISTRY_NAME);
  }

  protected void mvnDeployExpectUnauthorized(final Path settingsXml,
                                             final File projectPath)
    throws Exception {
    mvnDeployExpectUnauthorized(settingsXml, projectPath, HOSTED_REGISTRY_NAME);
  }

  protected void mvnDeploy(final Path settingsXml,
                           final File projectPath,
                           final String registryName)
    throws Exception {
    mvnDeploy(0, null, null, settingsXml, projectPath, registryName);
  }

  protected void mvnDeployExpectUnauthorized(final Path settingsXml,
                                             final File projectPath,
                                             final String registryName)
    throws Exception {
    mvnDeploy(1,
              "unauthorized",
              7,
              settingsXml,
              projectPath,
              registryName);
  }

  protected void mvnInstall(final Path settingsXml,
                            final File projectPath)
    throws Exception {
    mvnInstall(0,
               null,
               settingsXml,
               projectPath);
  }

  protected void mvnInstall(final int expectedRetCode,
                            final String errorMessageExtract,
                            final Path settingsXml,
                            final File projectPath)
    throws Exception {
    execute(expectedRetCode,
            errorMessageExtract,
            7,
            "mvn",
            "install",
            "-U",
            "-s",
            settingsXml.toAbsolutePath().toString(),
            "-f",
            projectPath.getAbsolutePath());
  }

  protected void replaceFilePatterns(final File file,
                                     final String registryName,
                                     final String username,
                                     final String password)
    throws IOException {
    final Path tempFile = Files.createTempFile(Paths.get("."), "temp", ".xml");
    Files.move(file.toPath(), tempFile, StandardCopyOption.REPLACE_EXISTING);
    replaceFilePatterns(tempFile.toFile(), file, registryName, username, password);
  }

  protected <T> T curlMetadata(final String registryName,
                               final String username,
                               final String password,
                               final String groupId,
                               final String artifactId,
                               final Class<T> resultType)
    throws Exception {
    final String groupPath = groupId.replace('.', '/');
    final String url = String.format("http://localhost:%d/registry/%s/%s/%s/maven-metadata.xml",
                                     port,
                                     registryName,
                                     groupPath,
                                     artifactId);
    return doCurl(username, password, url, resultType);
  }

  private Path getSettingsFile(final Path settingsXmlFile,
                               final String registryName,
                               final String username,
                               final String password)
    throws IOException {
    final Path tempSettingsFile = Files.createTempFile(Paths.get("."), SETTINGS_FILE_PREFIX, ".xml");
    replaceFilePatterns(settingsXmlFile.toFile(),
                        tempSettingsFile.toFile(),
                        registryName,
                        username,
                        password);
    return tempSettingsFile;
  }

  private void replaceFilePatterns(final File sourceFile,
                                   final File targetFile,
                                   final String registryName,
                                   final String username,
                                   final String password)
    throws IOException {
    final String registryUrl = String.format("http://localhost:%s/registry/%s", port, registryName);
    final List<String> lines = Files.readAllLines(sourceFile.toPath());
    try (PrintWriter printWriter = new PrintWriter(targetFile)) {
      for (final String line : lines) {
        final String newLine =
          line.replaceAll("@REGISTRY_URL@", registryUrl)
            .replaceAll("@USERNAME@", username)
            .replaceAll("@PASSWORD@", password);
        printWriter.println(newLine);
      }
    }
  }

  private void mvnDeploy(final int expectedRetCode,
                         final String errorMessageExtract,
                         final Integer fromEndErrorLine,
                         final Path settingsXml,
                         final File projectPath,
                         final String registryName)
    throws Exception {
    execute(expectedRetCode,
            errorMessageExtract,
            fromEndErrorLine,
            "mvn",
            "deploy",
            String.format("-DaltDeploymentRepository=registree::default::http://localhost:%d/registry/%s", port, registryName),
            "-s",
            settingsXml.toAbsolutePath().toString(),
            "-f",
            projectPath.getAbsolutePath());
  }

  private <T> T doCurl(final String username,
                       final String password,
                       final String url,
                       final Class<T> resultType)
    throws Exception {
    final Path result = Files.createTempFile(Paths.get("."), "curl", ".xml");
    final List<String> command = new ArrayList<>();
    command.add("curl");
    command.add("--fail-with-body");
    command.add("-o");
    command.add(result.toAbsolutePath().toString());
    if (username != null) {
      command.add("-u");
      command.add(String.format("%s:%s", username, password));
    }
    command.add(url);

    execute(command.toArray(new String[command.size()]));

    return registreeXmlConfiguration.getXmlMapper().readValue(result.toFile(), resultType);
  }
}
