package com.itesoft.registree.maven.rest.proxy;

import static com.itesoft.registree.maven.rest.proxy.ProxyHelper.toPath;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.java.CheckedConsumer;
import com.itesoft.registree.java.CheckedRunnable;
import com.itesoft.registree.java.CheckedSupplier;
import com.itesoft.registree.maven.dto.ArtifactCreation;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.maven.rest.MavenArtifactManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.rest.ReadOnlyMavenArtifactManager;
import com.itesoft.registree.maven.storage.ArtifactStorage;
import com.itesoft.registree.maven.storage.ChecksumStorage;
import com.itesoft.registree.registry.filtering.ProxyFilteringService;

import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class ProxyArtifactManager extends ReadOnlyMavenArtifactManager implements MavenArtifactManager {
  private static final String GET_ARTIFACT_URI = "%s/%s/%s/%s/%s";
  private static final String GET_MD5_URI = GET_ARTIFACT_URI + ".md5";

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyArtifactManager.class);

  @Autowired
  private ArtifactStorage artifactStorage;

  @Autowired
  private ChecksumStorage checksumStorage;

  @Autowired
  private ProxyHelper proxyHelper;

  @Autowired
  private ProxyFilteringService filteringService;

  @Override
  public RegistryType getType() {
    return RegistryType.PROXY;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> artifactExists(final MavenOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String groupId,
                                                              final String artifactId,
                                                              final String version,
                                                              final String fileName)
    throws Exception {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) context.getRegistry();
    final String groupPath = groupId.replace('.', '/');

    final CheckedSupplier<Boolean, Exception> includedSupplier = () -> {
      final String path = toPath(groupPath, artifactId, version);
      return filteringService.included(proxyRegistry,
                                       path);
    };

    final Supplier<String> cacheKeySupplier = () ->
      String.format("%s/%s/%s/%s", groupPath, artifactId, version, fileName);

    final CheckedSupplier<URI, Exception> fileUriSupplier = () -> {
      final URIBuilder artifactUriBuilder =
        new URIBuilder(String.format(GET_ARTIFACT_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId,
                                     version,
                                     fileName));
      return artifactUriBuilder.build();
    };

    return proxyHelper.fileExists(context,
                                  includedSupplier,
                                  cacheKeySupplier,
                                  fileUriSupplier,
                                  () -> artifactStorage.getArtifactFile(context.getRegistry(),
                                                                        groupId,
                                                                        artifactId,
                                                                        version,
                                                                        fileName));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getArtifact(final MavenOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String groupId,
                                                           final String artifactId,
                                                           final String version,
                                                           final String fileName)
    throws Exception {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) context.getRegistry();
    final String groupPath = groupId.replace('.', '/');

    final CheckedSupplier<Boolean, Exception> includedSupplier = () -> {
      final String path = toPath(groupPath, artifactId, version);
      return filteringService.included(proxyRegistry,
                                       path);
    };

    final Supplier<String> cacheKeySupplier = () ->
      String.format("%s/%s/%s/%s", groupPath, artifactId, version, fileName);

    final CheckedSupplier<URI, Exception> fileUriSupplier = () -> {
      final URIBuilder fileUriBuilder =
        new URIBuilder(String.format(GET_ARTIFACT_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId,
                                     version,
                                     fileName));
      return fileUriBuilder.build();
    };

    final CheckedSupplier<URI, Exception> md5UriSupplier = () -> {
      final URIBuilder md5UriBuilder =
        new URIBuilder(String.format(GET_MD5_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId,
                                     version,
                                     fileName));
      return md5UriBuilder.build();
    };

    final CheckedSupplier<String, Exception> md5Supplier = () -> checksumStorage.getArtifactChecksum(context.getRegistry(),
                                                                                                     groupId,
                                                                                                     artifactId,
                                                                                                     version,
                                                                                                     fileName + ".md5");

    final CheckedConsumer<String, Exception> publishMd5Consumer = (md5) -> checksumStorage.publishArtifactChecksum(context.getRegistry(),
                                                                                                                   groupId,
                                                                                                                   artifactId,
                                                                                                                   version,
                                                                                                                   fileName + ".md5",
                                                                                                                   md5);

    final Consumer<ClassicHttpResponse> proxyErrorMessageConsumer = (proxyResponse) -> {
      final Registry registry = context.getRegistry();
      LOGGER.error("[{}] Proxy answered with code {} when getting artifact {}:{}:{}",
                   registry.getName(),
                   proxyResponse.getCode(),
                   groupId,
                   artifactId,
                   version);
    };

    final CheckedRunnable prepareCreationRunnable = () -> artifactStorage.prepareArtifactCreation(context.getRegistry(),
                                                                                                  groupId,
                                                                                                  artifactId,
                                                                                                  version,
                                                                                                  fileName);

    final CheckedSupplier<ArtifactCreation, Exception> initiateCreationSupplier = () -> artifactStorage.initiateArtifactCreation(context.getRegistry(),
                                                                                                                                 groupId,
                                                                                                                                 artifactId,
                                                                                                                                 version,
                                                                                                                                 fileName);
    final CheckedConsumer<ArtifactCreation, Exception> createFileConsumer =
      (artifactCreation) -> artifactStorage.createArtifact(context.getRegistry(), artifactCreation);
    final Consumer<ArtifactCreation> abortFileCreationConsumer =
      (artifactCreation) -> artifactStorage.abortArtifactCreation(context.getRegistry(), artifactCreation);
    final CheckedSupplier<ResponseEntity<StreamingResponseBody>, Exception> getLocalFileSupplier = () -> {
      final MavenFile artifactFile =
        artifactStorage.getArtifactFile(context.getRegistry(),
                                        groupId,
                                        artifactId,
                                        version,
                                        fileName);
      return getLocalFile(artifactFile,
                          () -> String.format("Artifact %s:%s:%s not found",
                                              groupId,
                                              artifactId,
                                              version));
    };

    return proxyHelper.getFile(context,
                               includedSupplier,
                               cacheKeySupplier,
                               fileUriSupplier,
                               md5UriSupplier,
                               md5Supplier,
                               publishMd5Consumer,
                               proxyErrorMessageConsumer,
                               prepareCreationRunnable,
                               initiateCreationSupplier,
                               createFileConsumer,
                               abortFileCreationConsumer,
                               getLocalFileSupplier);
  }
}
