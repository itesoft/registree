package com.itesoft.registree.raw.test;

import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itesoft.registree.spring.test.RegistryTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;

public abstract class RawRegistryTest extends RegistryTest {
  // CHECKSTYLE:OFF
  protected final Path exceptionLib = Paths.get(RESOURCES_FOLDER, "raw/exception-25.3.3-master.tgz");
  protected final Path rpcLib = Paths.get(RESOURCES_FOLDER, "raw/rpc-25.3.3-master.tgz");
  // CHECKSTYLE:ON

  @BeforeAll
  public static synchronized void prepareResources() throws IOException {
    prepareResources(RawRegistryTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
  }

  @Override
  protected String getFormat() {
    return "raw";
  }

  protected void createHostedRegistry() throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("storagePath", "registry-hosted");
    createHostedRegistry(configurationAsMap);
  }

  protected void createGroupRegistry() throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("memberNames", Arrays.asList(HOSTED_REGISTRY_NAME));
    createGroupRegistry(configurationAsMap);
  }

  protected void publishThenGetAndCheckFile(final Path file,
                                            final String registryName,
                                            final String path,
                                            final String fileName)
    throws Exception {
    publishThenGetAndCheckFile(file, registryName, path, fileName, null, null, null);
  }

  protected void publishThenGetAndCheckFile(final Path file,
                                            final String registryName,
                                            final String path,
                                            final String fileName,
                                            final String contentType,
                                            final String username,
                                            final String password)
    throws Exception {
    publishFile(file.toAbsolutePath().toString(),
                registryName,
                path,
                fileName,
                contentType,
                username,
                password);
    getAndCheckFile(file,
                    registryName,
                    path,
                    fileName,
                    contentType,
                    username,
                    password);
  }

  protected void publishFile(final String localFilePath,
                             final String registryName,
                             final String path,
                             final String fileName)
    throws Exception {
    publishFile(localFilePath,
                registryName,
                path,
                fileName,
                null,
                null,
                null);
  }

  protected void publishFile(final String localFilePath,
                             final String registryName,
                             final String path,
                             final String fileName,
                             final String contentType,
                             final String username,
                             final String password)
    throws Exception {
    final String uri = getUri(registryName,
                              path,
                              fileName);
    final List<String> command = new ArrayList<>();
    command.add("curl");
    command.add("-f");
    command.add("--output");
    command.add("/dev/stdout");
    command.add("--upload-file");
    command.add(localFilePath);
    if (contentType != null) {
      command.add("--header");
      command.add("content-type: " + contentType);
    }
    if (username != null) {
      command.add("--user");
      command.add(username + ":" + password);
    }
    command.add(uri);
    execute(command.toArray(new String[command.size()]));
  }

  protected void getAndCheckFile(final Path file,
                                 final String registryName,
                                 final String path,
                                 final String fileName,
                                 final String contentType)
    throws Exception {
    getAndCheckFile(file,
                    registryName,
                    path,
                    fileName,
                    contentType,
                    null,
                    null);
  }

  protected void getAndCheckFile(final Path file,
                                 final String registryName,
                                 final String path,
                                 final String fileName,
                                 final String contentType,
                                 final String username,
                                 final String password)
    throws Exception {
    downloadFile(registryName,
                 path,
                 fileName,
                 username,
                 password);
    assertTrue(Files.isRegularFile(Paths.get(fileName)));
    assertArrayEquals(Files.readAllBytes(file),
                      Files.readAllBytes(Paths.get(fileName)));
  }

  protected void downloadFile(final String registryName,
                              final String path,
                              final String fileName)
    throws Exception {
    downloadFile(registryName, path, fileName, null, null);
  }

  protected void downloadFile(final String registryName,
                              final String path,
                              final String fileName,
                              final String username,
                              final String password)
    throws Exception {
    downloadFile(0,
                 null,
                 registryName,
                 path,
                 fileName,
                 username,
                 password);
  }

  protected void downloadFile(final int expectedRetcode,
                              final String errorMessageExtract,
                              final String registryName,
                              final String path,
                              final String fileName)
    throws Exception {
    downloadFile(expectedRetcode,
                 errorMessageExtract,
                 registryName,
                 path,
                 fileName,
                 null,
                 null);
  }

  protected void downloadFile(final int expectedRetcode,
                              final String errorMessageExtract,
                              final String registryName,
                              final String path,
                              final String fileName,
                              final String username,
                              final String password)
    throws Exception {
    final String uri = getUri(registryName,
                              path,
                              fileName);

    final List<String> command = new ArrayList<>();
    command.add("wget");
    command.add("-nv");
    command.add("-O");
    command.add(fileName);
    if (username != null) {
      command.add("--auth-no-challenge");
      command.add("--user=" + username);
      command.add("--password=" + password);
    }
    command.add(uri);
    final String[] commandArray = command.toArray(new String[command.size()]);
    execute(expectedRetcode,
            errorMessageExtract,
            1,
            commandArray);
  }

  private String getUri(final String registryName,
                        final String path,
                        final String fileName) {
    return String.format("http://localhost:%s/registry/%s/%s/%s",
                         port,
                         registryName,
                         path,
                         fileName);
  }
}
