package com.itesoft.registree.spring.test;

import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;
import static com.itesoft.registree.rest.test.Constants.API_URL_PREFIX;
import static com.itesoft.registree.rest.test.RouteHelper.createAnonymousApiReadWriteDeleteRoute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jakarta.ws.rs.client.ClientBuilder;

import com.itesoft.registree.configuration.RegistreeDataConfiguration;
import com.itesoft.registree.dto.CreateRegistryArgs;
import com.itesoft.registree.dto.CreateRouteArgs;
import com.itesoft.registree.dto.CreateUserArgs;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.ProxyRegistryFilter;
import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.rest.test.RegistryClient;
import com.itesoft.registree.rest.test.UserClient;
import com.itesoft.registree.rest.test.UserRouteClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.util.FileSystemUtils;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RegistryTest {
  protected static final String RESOURCES_FOLDER = "resources";
  protected static final String HOSTED_REGISTRY_NAME = "hosted";
  protected static final String PROXY_REGISTRY_NAME = "proxy";
  protected static final String GROUP_REGISTRY_NAME = "group";

  private static Set<String> preparedResources = new HashSet<>();

  // CHECKSTYLE:OFF
  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected RegistreeDataConfiguration registreeDataConfiguration;

  // FIXME: @LocalServerPort is not giving the right port
  protected final int port = 8080;
  // CHECKSTYLE:ON

  private RegistryClient registryClient;
  private UserClient userClient;
  private UserRouteClient userRouteClient;

  protected static synchronized void prepareResources(final String path) throws IOException {
    if (preparedResources.contains(path)) {
      return;
    }

    final File resourcesFolder = Paths.get(RESOURCES_FOLDER).toFile();
    FileSystemUtils.deleteRecursively(resourcesFolder);
    final File file = new File(path);
    if (file.isDirectory()) {
      FileSystemUtils.copyRecursively(file, resourcesFolder);
    } else {
      unzip(file, resourcesFolder);
    }
    preparedResources.add(path);
  }

  protected static void unzip(final File file,
                              final File destination)
    throws IOException {
    try (ZipFile zipFile = new ZipFile(file)) {
      final Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final File entryDestination = new File(destination, entry.getName());
        if (entry.isDirectory()) {
          entryDestination.mkdirs();
        } else {
          entryDestination.getParentFile().mkdirs();
          try (InputStream in = zipFile.getInputStream(entry);
              OutputStream out = new FileOutputStream(entryDestination)) {
            IOUtils.copy(in, out);
          }
        }
      }
    }
  }

  @BeforeAll
  public void initResteasy() {
    final String server = "http://localhost:" + port;
    final ResteasyClient client = (ResteasyClient) ClientBuilder.newClient();
    final ResteasyWebTarget target = client.target(server + API_URL_PREFIX);
    registryClient = target.proxy(RegistryClient.class);
    userClient = target.proxy(UserClient.class);
    userRouteClient = target.proxy(UserRouteClient.class);

    createAnonymousApiReadWriteDeleteRoute(server);
  }

  @BeforeEach
  public void deleteExistingRegistry() throws IOException {
    for (final String registryPath : getRegistryPaths()) {
      FileSystemUtils.deleteRecursively(Paths.get(registreeDataConfiguration.getRegistriesPath(), registryPath));
    }
  }

  protected abstract String getFormat();

  protected abstract String[] getRegistryPaths();

  protected void createHostedRegistry(final Map<String, Object> configurationAsMap)
    throws JsonProcessingException {
    createHostedRegistry(HOSTED_REGISTRY_NAME, configurationAsMap);
  }

  protected void createHostedRegistry(final String registryName,
                                      final Map<String, Object> configurationAsMap)
    throws JsonProcessingException {
    final String configuration = objectMapper.writeValueAsString(configurationAsMap);

    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .format(getFormat())
      .type(RegistryType.HOSTED.name())
      .name(registryName)
      .configuration(configuration)
      .build();
    registryClient.createRegistry(createRegistryArgs);
  }

  protected void createProxyRegistry(final String proxyUrl)
    throws JsonProcessingException {
    createProxyRegistry(true, proxyUrl, 0, null, null);
  }

  protected void createProxyRegistry(final String proxyUrl,
                                     final ProxyRegistryFiltering filtering)
    throws JsonProcessingException {
    createProxyRegistry(true, proxyUrl, 0, filtering, null);
  }

  protected void createProxyRegistry(final boolean doStore,
                                     final String proxyUrl,
                                     final int cacheTimeout,
                                     final ProxyRegistryFiltering proxyRegistryFiltering,
                                     final Map<String, Object> additionalConfiguration)
    throws JsonProcessingException {
    final Map<String, Object> configurationAsMap = new HashMap<>();
    if (doStore) {
      configurationAsMap.put("storagePath", "registry-proxy");
    }
    configurationAsMap.put("doStore", doStore);
    configurationAsMap.put("proxyUrl", proxyUrl);
    if (proxyRegistryFiltering != null) {
      configurationAsMap.put("filtering", proxyRegistryFiltering);
    }
    if (additionalConfiguration != null) {
      configurationAsMap.putAll(additionalConfiguration);
    }
    createProxyRegistry(configurationAsMap);
  }

  protected void createProxyRegistry(final Map<String, Object> configurationAsMap)
    throws JsonProcessingException {
    final String configuration = objectMapper.writeValueAsString(configurationAsMap);

    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .format(getFormat())
      .type(RegistryType.PROXY.getValue())
      .name(PROXY_REGISTRY_NAME)
      .configuration(configuration)
      .build();
    registryClient.createRegistry(createRegistryArgs);
  }

  protected void createGroupRegistry(final Map<String, Object> configurationAsMap)
    throws JsonProcessingException {
    final String configuration = objectMapper.writeValueAsString(configurationAsMap);

    final CreateRegistryArgs createRegistryArgs = CreateRegistryArgs.builder()
      .format(getFormat())
      .type(RegistryType.GROUP.name())
      .name(GROUP_REGISTRY_NAME)
      .configuration(configuration)
      .build();
    registryClient.createRegistry(createRegistryArgs);
  }

  protected ProxyRegistryFiltering createProxyRegistryFiltering(final List<String> pathPrefixes,
                                                                final ProxyRegistryFilterPolicy policy,
                                                                final ProxyRegistryFilterPolicy defaultPolicy) {
    final List<ProxyRegistryFilter> filters = new ArrayList<>();
    for (final String pathPrefix : pathPrefixes) {
      final ProxyRegistryFilter filter = new ProxyRegistryFilter();
      filter.setPathPrefix(pathPrefix);
      filter.setPolicy(policy);
      filters.add(filter);
    }
    final ProxyRegistryFiltering filtering = new ProxyRegistryFiltering();
    filtering.setFilters(filters);
    filtering.setDefaultPolicy(defaultPolicy);
    return filtering;
  }

  protected void createUser(final String username, final String password) {
    final CreateUserArgs createUserArgs = CreateUserArgs.builder()
      .username(username)
      .password(password)
      .build();
    userClient.createUser(createUserArgs);
  }

  protected void createAnonymousHostedReadWriteRoute() {
    createAnonymousHostedReadWriteRoute(HOSTED_REGISTRY_NAME);
  }

  protected void createAnonymousHostedReadWriteRoute(final String registryName) {
    createRoute(ANONYMOUS_USERNAME,
                "/" + registryName,
                "rw");
  }

  protected void createAnonymousProxyReadRoute() {
    createAnonymousReadRoute("/" + PROXY_REGISTRY_NAME);
  }

  protected void createAnonymousGroupReadRoute() {
    createAnonymousReadRoute("/" + GROUP_REGISTRY_NAME);
  }

  protected void createAnonymousReadRoute(final String path) {
    createRoute(ANONYMOUS_USERNAME,
                path,
                "r");
  }

  protected void createRoute(final String username,
                             final String path,
                             final String permissions) {
    userRouteClient.createRoute(OneOfLongOrString.from(username),
                                path,
                                CreateRouteArgs.builder()
                                  .permissions(permissions)
                                  .build());
  }
}
