package com.itesoft.registree.npm.test;

import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;
import static com.itesoft.registree.test.CommandExecutionHelper.execute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.itesoft.registree.dto.ProxyRegistryFiltering;
import com.itesoft.registree.spring.test.RegistryTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.util.FileSystemUtils;

public abstract class NpmRegistryTest extends RegistryTest {
  protected static final String NODE_MODULES_FOLDER_NAME = "node_modules";
  private static final String PACKAGE_JSON_FILE_NAME = "package.json";
  private static final String PACKAGE_LOCK_JSON_FILE_NAME = "package-lock.json";
  private static final String NPM_CACHE_FOLDER = "npm-cache";
  private static final String NPM_CACHE_PARAMETER = "cache=" + NPM_CACHE_FOLDER;

  // CHECKSTYLE:OFF
  protected final Path rpc250303Library = Paths.get(RESOURCES_FOLDER, "npm/rpc-25.3.3-master.tgz");
  protected final Path rpc250304Library = Paths.get(RESOURCES_FOLDER, "npm/rpc-25.3.4-master.tgz");
  protected final Path exception250303Library = Paths.get(RESOURCES_FOLDER, "npm/exception-25.3.3-master.tgz");
  // CHECKSTYLE:ON

  @BeforeAll
  public static synchronized void prepareResources() throws IOException {
    prepareResources(NpmRegistryTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
  }

  @BeforeEach
  public void cleanNpmStuff() throws IOException {
    FileSystemUtils.deleteRecursively(Paths.get(NPM_CACHE_FOLDER));
    cleanPreviousNpmInstall();
  }

  @Override
  protected String getFormat() {
    return "npm";
  }

  protected void cleanPreviousNpmInstall() throws IOException {
    FileSystemUtils.deleteRecursively(Paths.get(NODE_MODULES_FOLDER_NAME));
    Files.deleteIfExists(Paths.get(PACKAGE_JSON_FILE_NAME));
    Files.deleteIfExists(Paths.get(PACKAGE_LOCK_JSON_FILE_NAME));
  }

  protected void npmPublish(final String npmrcPath,
                            final Path file)
    throws Exception {
    npmPublish(0, null, npmrcPath, file);
  }

  protected void npmPublish(final int expectedRetcode,
                            final String errorMessageExtract,
                            final String npmrcPath,
                            final Path file)
    throws Exception {
    execute(expectedRetcode,
            errorMessageExtract,
            3,
            "npm",
            "publish",
            "--userconfig=" + npmrcPath,
            file.toAbsolutePath().toString());
  }

  protected void npmView(final String npmrcPath,
                         final String lib)
    throws Exception {
    execute("npm", "view", "--userconfig=" + npmrcPath, lib);
  }

  protected void npmInstall(final String npmrcPath,
                            final String lib)
    throws Exception {
    execute("npm", "install", "--userconfig=" + npmrcPath, lib);
  }

  protected void npmInstall(final int expectedRetcode,
                            final String errorMessageExtract,
                            final String npmrcPath,
                            final String lib)
    throws Exception {
    execute(expectedRetcode,
            errorMessageExtract,
            7,
            "npm",
            "install",
            "--userconfig=" + npmrcPath,
            lib);
  }

  protected <T> T curlPackageJson(final String registryName,
                                  final String lib,
                                  final Class<T> resultType)
    throws Exception {
    return curlPackageJson(registryName,
                           null,
                           null,
                           lib,
                           resultType);
  }

  protected <T> T curlPackageJson(final String registryName,
                                  final String username,
                                  final String password,
                                  final String lib,
                                  final Class<T> resultType)
    throws Exception {
    final String registryWebPath = getRegistryWebPath(registryName);
    final String url = String.format("http:%s%s",
                                     registryWebPath,
                                     lib);
    return doCurl(username, password, url, resultType);
  }

  protected <T> T curlSpecificVersion(final String registryName,
                                      final String lib,
                                      final String version,
                                      final Class<T> resultType)
    throws Exception {
    return curlSpecificVersion(registryName,
                               null,
                               null,
                               lib,
                               version,
                               resultType);
  }

  protected <T> T curlSpecificVersion(final String registryName,
                                      final String username,
                                      final String password,
                                      final String lib,
                                      final String version,
                                      final Class<T> resultType)
    throws Exception {
    final String registryWebPath = getRegistryWebPath(registryName);
    final String url = String.format("http:%s%s/%s",
                                     registryWebPath,
                                     lib,
                                     version);
    return doCurl(username, password, url, resultType);
  }

  protected String createAnonymousNpmrc(final String registryName)
    throws Exception {
    return createNpmrc(registryName,
                       ANONYMOUS_USERNAME,
                       "useless");
  }

  protected String createNpmrc(final String registryName,
                               final String username,
                               final String password)
    throws Exception {
    return doCreateNpmrc(registryName,
                         username,
                         Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes()));
  }

  protected void createHostedRegistry() throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("storagePath", "registry-hosted");
    createHostedRegistry(configurationAsMap);
  }
  protected void createProxyRegistry()
    throws JsonProcessingException {
    createProxyRegistry(true, 0);
  }

  protected void createProxyRegistry(final int cacheTimeout)
    throws JsonProcessingException {
    createProxyRegistry(true, cacheTimeout);
  }

  protected void createProxyRegistry(final ProxyRegistryFiltering proxyRegistryFiltering)
    throws JsonProcessingException {
    createProxyRegistry(true, "https://registry.npmjs.org", 0, proxyRegistryFiltering, null);
  }

  protected void createProxyRegistry(final boolean doStore,
                                     final int cacheTimeout)
    throws JsonProcessingException {
    createProxyRegistry(doStore, "https://registry.npmjs.org", cacheTimeout, null, null);
  }

  protected void createGroupRegistry() throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    configurationAsMap.put("memberNames", Arrays.asList(HOSTED_REGISTRY_NAME, PROXY_REGISTRY_NAME));
    createGroupRegistry(configurationAsMap);
  }

  private String getRegistryWebPath(final String registryName) {
    return String.format("//localhost:%d/registry/%s/",
                         port,
                         registryName);
  }

  private <T> T doCurl(final String username,
                       final String password,
                       final String url,
                       final Class<T> resultType)
    throws Exception {
    final Path result = Files.createTempFile(Paths.get("."), "curl", ".json");
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

    return objectMapper.readValue(result.toFile(), resultType);
  }

  private String doCreateNpmrc(final String registryName,
                               final String username,
                               final String authToken)
    throws Exception {
    final String registryWebPath = getRegistryWebPath(registryName);
    final String auth = String.format("%s:_authToken=\"%s\"",
                                      registryWebPath,
                                      authToken);
    final String registry = String.format("registry=http:%s",
                                          registryWebPath);
    final String npmrcContent = String.format("%s\n%s\n\n%s",
                                              registry,
                                              auth,
                                              NPM_CACHE_PARAMETER);
    return Files.writeString(Paths.get("npmrc." + registryName + "." + username),
                             npmrcContent)
      .toAbsolutePath()
      .toString();
  }
}
