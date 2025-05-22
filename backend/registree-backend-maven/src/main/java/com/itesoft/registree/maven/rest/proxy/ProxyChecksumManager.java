package com.itesoft.registree.maven.rest.proxy;

import static com.itesoft.registree.maven.rest.proxy.ProxyHelper.toPath;

import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.java.CheckedConsumer;
import com.itesoft.registree.java.CheckedFunction;
import com.itesoft.registree.java.CheckedSupplier;
import com.itesoft.registree.maven.rest.MavenChecksumManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.rest.ReadOnlyMavenChecksumManager;
import com.itesoft.registree.maven.rest.error.MavenErrorManager;
import com.itesoft.registree.maven.storage.ChecksumStorage;
import com.itesoft.registree.proxy.HttpHelper;
import com.itesoft.registree.registry.filtering.ProxyFilteringService;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class ProxyChecksumManager extends ReadOnlyMavenChecksumManager implements MavenChecksumManager {
  private static final String GET_METADATA_CHECKSUM_URI = "%s/%s/%s/%s";
  private static final String GET_ARTIFACT_CHECKSUM_URI = "%s/%s/%s/%s/%s";

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyChecksumManager.class);

  @Autowired
  private ProxyHelper proxyHelper;

  @Autowired
  private MavenErrorManager errorManager;

  @Autowired
  private ChecksumStorage checksumStorage;

  @Autowired
  private ProxyFilteringService filteringService;

  @Autowired
  private HttpHelper httpHelper;

  @Override
  public RegistryType getType() {
    return RegistryType.PROXY;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> metadataChecksumExists(final MavenOperationContext context,
                                                                      final HttpServletRequest request,
                                                                      final String groupId,
                                                                      final String artifactId,
                                                                      final String fileName,
                                                                      final String extension)
    throws Exception {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) context.getRegistry();
    final String groupPath = groupId.replace('.', '/');

    final CheckedSupplier<Boolean, Exception> includedSupplier = () -> {
      final String path = toPath(groupPath, artifactId);
      return filteringService.included(proxyRegistry,
                                       path);
    };

    final Supplier<String> cacheKeySupplier = () ->
      String.format("%s/%s/%s", groupPath, artifactId, fileName);

    final CheckedSupplier<URI, Exception> checksumUriSupplier = () -> {
      final URIBuilder checksumUriBuilder =
        new URIBuilder(String.format(GET_METADATA_CHECKSUM_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId,
                                     fileName));
      return checksumUriBuilder.build();
    };

    return checksumExists(context,
                          includedSupplier,
                          cacheKeySupplier,
                          checksumUriSupplier,
                          () -> checksumStorage.getMetadataChecksum(context.getRegistry(),
                                                                    groupId,
                                                                    artifactId,
                                                                    fileName));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> artifactChecksumExists(final MavenOperationContext context,
                                                                      final HttpServletRequest request,
                                                                      final String groupId,
                                                                      final String artifactId,
                                                                      final String version,
                                                                      final String fileName,
                                                                      final String extension)
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

    final CheckedSupplier<URI, Exception> checksumUriSupplier = () -> {
      final URIBuilder checksumUriBuilder =
        new URIBuilder(String.format(GET_ARTIFACT_CHECKSUM_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId,
                                     version,
                                     fileName));
      return checksumUriBuilder.build();
    };

    return checksumExists(context,
                          includedSupplier,
                          cacheKeySupplier,
                          checksumUriSupplier,
                          () -> checksumStorage.getArtifactChecksum(context.getRegistry(),
                                                                    groupId,
                                                                    artifactId,
                                                                    version,
                                                                    fileName));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getMetadataChecksum(final MavenOperationContext context,
                                                                   final HttpServletRequest request,
                                                                   final String groupId,
                                                                   final String artifactId,
                                                                   final String fileName,
                                                                   final String extension)
    throws Exception {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) context.getRegistry();
    final String groupPath = groupId.replace('.', '/');

    final CheckedSupplier<Boolean, Exception> includedSupplier = () -> {
      final String path = toPath(groupPath, artifactId);
      return filteringService.included(proxyRegistry,
                                       path);
    };

    final Supplier<String> cacheKeySupplier = () ->
      String.format("%s/%s/%s", groupPath, artifactId, fileName);

    final CheckedSupplier<URI, Exception> checksumUriSupplier = () -> {
      final URIBuilder checksumUriBuilder =
        new URIBuilder(String.format(GET_METADATA_CHECKSUM_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId,
                                     fileName));
      return checksumUriBuilder.build();
    };

    final Consumer<ClassicHttpResponse> proxyErrorMessageConsumer = (response) -> {
      final Registry registry = context.getRegistry();
      LOGGER.error("[{}] Proxy answered with code {} when getting metadata {}:{}",
                   registry.getName(),
                   response.getCode(),
                   groupId,
                   artifactId);
    };

    final CheckedConsumer<String, Exception> remoteChecksumConsumer =
      (checksum) -> checksumStorage.publishMetadataChecksum(context.getRegistry(),
                                                            groupId,
                                                            artifactId,
                                                            fileName,
                                                            checksum);

    final CheckedSupplier<String, Exception> localChecksumSupplier =
      () -> checksumStorage.getMetadataChecksum(context.getRegistry(),
                                                groupId,
                                                artifactId,
                                                fileName);

    final Supplier<String> errorMessageSupplier =
      () -> String.format("Checksum %s for metadata %s:%s not found",
                          fileName,
                          groupId,
                          artifactId);

    return getChecksum(context,
                       includedSupplier,
                       cacheKeySupplier,
                       checksumUriSupplier,
                       proxyErrorMessageConsumer,
                       remoteChecksumConsumer,
                       localChecksumSupplier,
                       errorMessageSupplier);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getArtifactChecksum(final MavenOperationContext context,
                                                                   final HttpServletRequest request,
                                                                   final String groupId,
                                                                   final String artifactId,
                                                                   final String version,
                                                                   final String fileName,
                                                                   final String extension)
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

    final CheckedSupplier<URI, Exception> checksumUriSupplier = () -> {
      final URIBuilder checksumUriBuilder =
        new URIBuilder(String.format(GET_ARTIFACT_CHECKSUM_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId,
                                     version,
                                     fileName));
      return checksumUriBuilder.build();
    };

    final Consumer<ClassicHttpResponse> proxyErrorMessageConsumer = (response) -> {
      final Registry registry = context.getRegistry();
      LOGGER.error("[{}] Proxy answered with code {} when getting artifact {}:{}:{}",
                   registry.getName(),
                   response.getCode(),
                   groupId,
                   artifactId,
                   version);
    };

    final CheckedConsumer<String, Exception> remoteChecksumConsumer =
      (checksum) -> checksumStorage.publishArtifactChecksum(context.getRegistry(),
                                                            groupId,
                                                            artifactId,
                                                            version,
                                                            fileName,
                                                            checksum);

    final CheckedSupplier<String, Exception> localChecksumSupplier =
      () -> checksumStorage.getArtifactChecksum(context.getRegistry(),
                                                groupId,
                                                artifactId,
                                                version,
                                                fileName);

    final Supplier<String> errorMessageSupplier =
      () -> String.format("Checksum %s for artifact %s:%s:%s not found",
                          fileName,
                          groupId,
                          artifactId,
                          version);

    return getChecksum(context,
                       includedSupplier,
                       cacheKeySupplier,
                       checksumUriSupplier,
                       proxyErrorMessageConsumer,
                       remoteChecksumConsumer,
                       localChecksumSupplier,
                       errorMessageSupplier);
  }

  private ResponseEntity<StreamingResponseBody> checksumExists(final MavenOperationContext context,
                                                               final CheckedSupplier<Boolean, Exception> includedSupplier,
                                                               final Supplier<String> cacheKeySupplier,
                                                               final CheckedSupplier<URI, Exception> checksumUriSupplier,
                                                               final CheckedSupplier<String, Exception> localChecksumSupplier)
    throws Exception {
    final boolean included = includedSupplier.get();
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    final CheckedFunction<String, ResponseEntity<StreamingResponseBody>> okResultFunction = (checksum) -> {
      return ResponseEntity.ok().build();
    };

    if (proxyHelper.useCache(context, cacheKeySupplier)) {
      final String checksum = localChecksumSupplier.get();
      return toReponse(null,
                       okResultFunction,
                       checksum);
    }

    final URI checksumUri = checksumUriSupplier.get();
    final HttpHead httpHead = new HttpHead(checksumUri);

    return getChecksum(httpHead,
                       null,
                       null,
                       localChecksumSupplier,
                       null,
                       okResultFunction);
  }

  private ResponseEntity<StreamingResponseBody> getChecksum(final MavenOperationContext context,
                                                            final CheckedSupplier<Boolean, Exception> includedSupplier,
                                                            final Supplier<String> cacheKeySupplier,
                                                            final CheckedSupplier<URI, Exception> checksumUriSupplier,
                                                            final Consumer<ClassicHttpResponse> proxyErrorMessageConsumer,
                                                            final CheckedConsumer<String, Exception> remoteChecksumConsumer,
                                                            final CheckedSupplier<String, Exception> localChecksumSupplier,
                                                            final Supplier<String> errorMessageSupplier)
    throws Exception {
    final boolean included = includedSupplier.get();
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    final CheckedFunction<String, ResponseEntity<StreamingResponseBody>> okResultFunction = (checksum) -> {
      final StreamingResponseBody stream = outputStream -> {
        outputStream.write(checksum.getBytes());
      };

      return ResponseEntity.ok(stream);
    };

    if (proxyHelper.useCache(context, cacheKeySupplier)) {
      final String checksum = localChecksumSupplier.get();
      return toReponse(errorMessageSupplier,
                       okResultFunction,
                       checksum);
    }

    final URI checksumUri = checksumUriSupplier.get();
    final HttpGet httpGet = new HttpGet(checksumUri);

    return getChecksum(httpGet,
                       proxyErrorMessageConsumer,
                       remoteChecksumConsumer,
                       localChecksumSupplier,
                       errorMessageSupplier,
                       okResultFunction);
  }

  private ResponseEntity<StreamingResponseBody> getChecksum(final ClassicHttpRequest request,
                                                            final Consumer<ClassicHttpResponse> proxyErrorMessageConsumer,
                                                            final CheckedConsumer<String, Exception> remoteChecksumConsumer,
                                                            final CheckedSupplier<String, Exception> localChecksumSupplier,
                                                            final Supplier<String> errorMessageSupplier,
                                                            final CheckedFunction<String, ResponseEntity<StreamingResponseBody>> okResultFunction)
    throws Exception {
    final CloseableHttpClient httpClient = httpHelper.createHttpClient();
    try {
      String checksum =
        httpClient.execute(request, (response) -> {
          if (response.getCode() != HttpStatus.OK.value()) {
            if (proxyErrorMessageConsumer != null) {
              proxyErrorMessageConsumer.accept(response);
            }
            return null;
          }
          return EntityUtils.toString(response.getEntity());
        });

      if (checksum == null) {
        checksum = localChecksumSupplier.get();
      } else if (remoteChecksumConsumer != null) {
        remoteChecksumConsumer.accept(checksum);
      }

      return toReponse(errorMessageSupplier,
                       okResultFunction,
                       checksum);
    } finally {
      try {
        httpClient.close();
      } catch (final IOException exception) {
        LOGGER.error(exception.getMessage(), exception);
      }
    }
  }

  private ResponseEntity<StreamingResponseBody> toReponse(final Supplier<String> errorMessageSupplier,
                                                          final CheckedFunction<String, ResponseEntity<StreamingResponseBody>> okResultFunction,
                                                          final String checksum)
    throws Exception {
    if (checksum == null) {
      if (errorMessageSupplier != null) {
        return errorManager.getErrorResponse(HttpStatus.NOT_FOUND, errorMessageSupplier.get());
      } else {
        return ResponseEntity.notFound().build();
      }
    }

    return okResultFunction.apply(checksum);
  }
}
