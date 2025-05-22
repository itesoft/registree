package com.itesoft.registree.maven.rest.proxy;

import static com.itesoft.registree.maven.config.MavenConstants.METADATA_FILE_NAME;
import static com.itesoft.registree.maven.rest.proxy.ProxyHelper.toPath;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.java.CheckedConsumer;
import com.itesoft.registree.java.CheckedSupplier;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.maven.dto.MetadataCreation;
import com.itesoft.registree.maven.rest.MavenMetadataManager;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.maven.rest.ReadOnlyMavenMetadataManager;
import com.itesoft.registree.maven.storage.ChecksumStorage;
import com.itesoft.registree.maven.storage.MetadataStorage;
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
public class ProxyMetadataManager extends ReadOnlyMavenMetadataManager implements MavenMetadataManager {
  private static final String GET_METADATA_URI = "%s/%s/" + METADATA_FILE_NAME;
  private static final String GET_MD5_URI = GET_METADATA_URI + ".md5";

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyMetadataManager.class);

  @Autowired
  private MetadataStorage metadataStorage;

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
  public ResponseEntity<StreamingResponseBody> metadataExists(final MavenOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String groupId,
                                                              final String artifactId)
    throws Exception {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) context.getRegistry();
    final String groupPath = groupId.replace('.', '/');

    final CheckedSupplier<Boolean, Exception> includedSupplier = () -> {
      final String path = toPath(groupPath, artifactId);
      return filteringService.included(proxyRegistry,
                                       path);
    };

    final Supplier<String> cacheKeySupplier = () ->
      String.format("%s/%s/%s", groupPath, artifactId, METADATA_FILE_NAME);

    final CheckedSupplier<URI, Exception> fileUriSupplier = () -> {
      final URIBuilder metadataUriBuilder =
        new URIBuilder(String.format(GET_METADATA_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId));
      return metadataUriBuilder.build();
    };

    return proxyHelper.fileExists(context,
                                  includedSupplier,
                                  cacheKeySupplier,
                                  fileUriSupplier,
                                  () -> metadataStorage.getMetadataFile(context.getRegistry(),
                                                                        groupId,
                                                                        artifactId));
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getMetadata(final MavenOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String groupId,
                                                           final String artifactId)
    throws Exception {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) context.getRegistry();
    final String groupPath = groupId.replace('.', '/');

    final CheckedSupplier<Boolean, Exception> includedSupplier = () -> {
      final String path = toPath(groupPath, artifactId);
      return filteringService.included(proxyRegistry,
                                       path);
    };

    final Supplier<String> cacheKeySupplier = () ->
      String.format("%s/%s/%s", groupPath, artifactId, METADATA_FILE_NAME);

    final CheckedSupplier<URI, Exception> fileUriSupplier = () -> {
      final URIBuilder fileUriBuilder =
        new URIBuilder(String.format(GET_METADATA_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId));
      return fileUriBuilder.build();
    };

    final CheckedSupplier<URI, Exception> md5UriSupplier = () -> {
      final URIBuilder md5UriBuilder =
        new URIBuilder(String.format(GET_MD5_URI,
                                     proxyRegistry.getProxyUrl(),
                                     groupPath,
                                     artifactId));
      return md5UriBuilder.build();
    };

    final CheckedSupplier<String, Exception> md5Supplier = () -> checksumStorage.getMetadataChecksum(context.getRegistry(),
                                                                                                     groupId,
                                                                                                     artifactId,
                                                                                                     METADATA_FILE_NAME + ".md5");

    final CheckedConsumer<String, Exception> publishMd5Consumer = (md5) -> checksumStorage.publishMetadataChecksum(context.getRegistry(),
                                                                                                                   groupId,
                                                                                                                   artifactId,
                                                                                                                   METADATA_FILE_NAME + ".md5",
                                                                                                                   md5);

    final Consumer<ClassicHttpResponse> proxyErrorMessageConsumer = (proxyResponse) -> {
      final Registry registry = context.getRegistry();
      LOGGER.error("[{}] Proxy answered with code {} when getting metadata {}:{}",
                   registry.getName(),
                   proxyResponse.getCode(),
                   groupId,
                   artifactId);
    };
    final CheckedSupplier<MetadataCreation, Exception> initiateCreationSupplier = () -> metadataStorage.initiateMetadataCreation(context.getRegistry(),
                                                                                                                                 groupId,
                                                                                                                                 artifactId);
    final CheckedConsumer<MetadataCreation, Exception> createFileConsumer =
      (metadataCreation) -> metadataStorage.createMetadata(context.getRegistry(), metadataCreation);
    final Consumer<MetadataCreation> abortFileCreationConsumer =
      (metadataCreation) -> metadataStorage.abortMetadataCreation(context.getRegistry(), metadataCreation);
    final CheckedSupplier<ResponseEntity<StreamingResponseBody>, Exception> getLocalFileSupplier = () -> {
      final MavenFile metadataFile =
        metadataStorage.getMetadataFile(context.getRegistry(),
                                        groupId,
                                        artifactId);
      return getLocalFile(metadataFile,
                          () -> String.format("Metadata for %s:%s not found",
                                              groupId,
                                              artifactId));
    };

    return proxyHelper.getFile(context,
                               includedSupplier,
                               cacheKeySupplier,
                               fileUriSupplier,
                               md5UriSupplier,
                               md5Supplier,
                               publishMd5Consumer,
                               proxyErrorMessageConsumer,
                               null,
                               initiateCreationSupplier,
                               createFileConsumer,
                               abortFileCreationConsumer,
                               getLocalFileSupplier);
  }
}
