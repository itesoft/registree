package com.itesoft.registree.npm.rest.proxy;

import static com.itesoft.registree.IoHelper.closeSilently;
import static com.itesoft.registree.npm.storage.NpmHelper.extractVersionFromFileName;
import static com.itesoft.registree.npm.storage.PackageStorage.PACKAGE_JSON_FILENAME;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.CloseableCleaner;
import com.itesoft.registree.CloseableHolder;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.java.CheckedBiFunction;
import com.itesoft.registree.java.CheckedFunction;
import com.itesoft.registree.npm.config.NpmProxyRegistry;
import com.itesoft.registree.npm.dto.GetPackageResult;
import com.itesoft.registree.npm.dto.TarballCreation;
import com.itesoft.registree.npm.dto.json.ResponsePackage;
import com.itesoft.registree.npm.dto.json.Version;
import com.itesoft.registree.npm.rest.NpmOperationContext;
import com.itesoft.registree.npm.rest.NpmPackageManager;
import com.itesoft.registree.npm.rest.ReadOnlyNpmPackageManager;
import com.itesoft.registree.npm.rest.proxy.auth.NpmProxyAuthenticationManager;
import com.itesoft.registree.proxy.HttpHelper;
import com.itesoft.registree.proxy.ProxyCache;
import com.itesoft.registree.registry.api.storage.StorageHelper;
import com.itesoft.registree.registry.filtering.ProxyFilteringService;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ETag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class ProxyPackageManager extends ReadOnlyNpmPackageManager implements NpmPackageManager {
  private static final String GET_PACKAGE_URI = "%s/%s";
  private static final String GET_PACKAGE_VERSION_URI = "%s/%s/%s";
  private static final String DOWNLOAD_TARBALL_URI = "%s/%s/-/%s";

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyPackageManager.class);

  @Autowired
  private NpmProxyAuthenticationManager proxyAuthenticationManager;

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private ProxyFilteringService filteringService;

  @Autowired
  private CloseableCleaner closeableCleaner;

  @Autowired
  private HttpHelper httpHelper;

  @Autowired
  private ProxyCache proxyCache;

  @Override
  public RegistryType getType() {
    return RegistryType.PROXY;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getPackage(final NpmOperationContext context,
                                                          final HttpServletRequest request,
                                                          final String packageScope,
                                                          final String packageName)
    throws Exception {
    final NpmProxyRegistry proxyRegistry = (NpmProxyRegistry) context.getRegistry();
    final String packageFullName = getFullPackageName(packageScope, packageName);
    final boolean included = filteringService.included(proxyRegistry,
                                                       packageFullName);
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    final String cacheKey = String.format("%s/%s", packageFullName, PACKAGE_JSON_FILENAME);
    if (useCache(context, cacheKey)) {
      final String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
      if (ifNoneMatch != null) {
        final String checksum =
          getPackageStorage().getPackageChecksum(context.getRegistry(),
                                                 packageScope,
                                                 packageName);
        if (ifNoneMatch.equals(checksum)) {
          final HttpHeaders headers = createHeadersWithEtag(checksum);
          return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
        }
      }
      return getPackageLocal(context,
                             request,
                             packageScope,
                             packageName);
    }

    final String checksum;
    if (storageHelper.getDoStore(context.getRegistry())) {
      checksum = getPackageStorage().getPackageChecksum(context.getRegistry(),
                                                        packageScope,
                                                        packageName);
    } else {
      checksum = null;
    }

    final CheckedFunction<ClassicHttpResponse, ResponseEntity<StreamingResponseBody>> notModifiedFunction =
      proxyResponse -> {
        final HttpHeaders headers = createHeadersWithEtag(proxyResponse);
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
      };

    final CheckedFunction<ClassicHttpResponse, ResponseEntity<StreamingResponseBody>> errorFunction =
      proxyResponse -> {
        return ResponseEntity.status(HttpStatus.valueOf(proxyResponse.getCode())).build();
      };

    final CheckedFunction<ClassicHttpResponse, ResponseEntity<StreamingResponseBody>> checksumCheckingFunction =
      proxyResponse -> {
        if (checksum != null) {
          final String etag = getEtag(proxyResponse);
          if (checksum.equals(etag)) {
            final HttpHeaders headers = createHeadersWithEtag(proxyResponse);
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).build();
          }
        }
        return null;
      };

    final CheckedBiFunction<CloseableHttpClient, ClassicHttpResponse, ResponseEntity<StreamingResponseBody>> okFunction =
      (httpClient, proxyResponse) -> {
        final GetPackageResult getPackageResult =
          storePackageJson(context,
                           request,
                           packageScope,
                           packageName,
                           httpClient,
                           proxyResponse);

        return getPackage(getPackageResult);
      };

    final ResponseEntity<StreamingResponseBody> remoteResponse =
      getPackageRemote(context,
                       packageScope,
                       packageName,
                       checksum,
                       notModifiedFunction,
                       errorFunction,
                       checksumCheckingFunction,
                       okFunction);

    if (HttpStatus.NOT_MODIFIED.equals(remoteResponse.getStatusCode())) {
      final String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
      final String remoteEtag = remoteResponse.getHeaders().getETag();
      if (remoteEtag.equals(ifNoneMatch)) {
        return remoteResponse;
      } else {
        return getPackageLocal(context,
                               request,
                               packageScope,
                               packageName);
      }
    }

    return remoteResponse;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getPackageVersion(final NpmOperationContext context,
                                                                 final HttpServletRequest request,
                                                                 final String packageScope,
                                                                 final String packageName,
                                                                 final String packageVersion)
    throws Exception {
    final NpmProxyRegistry proxyRegistry = (NpmProxyRegistry) context.getRegistry();
    final String packageFullName = getFullPackageName(packageScope, packageName);
    final boolean included = filteringService.included(proxyRegistry,
                                                       packageFullName);
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    final String cacheKey = String.format("%s/%s", packageFullName, packageVersion);
    Version version = null;
    if (!useCache(context, cacheKey)) {
      version = getPackageVersionRemote(context,
                                        packageScope,
                                        packageName,
                                        packageVersion);
    }

    if (version == null) {
      version = getPackageVersionFromPackageJson(context,
                                                 request,
                                                 packageScope,
                                                 packageName,
                                                 packageVersion);
    }

    return getPackageVersion(context,
                             packageScope,
                             packageName,
                             version);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> downloadTarball(final NpmOperationContext context,
                                                               final HttpServletRequest request,
                                                               final String packageScope,
                                                               final String packageName,
                                                               final String fileName)
    throws Exception {
    final NpmProxyRegistry proxyRegistry = (NpmProxyRegistry) context.getRegistry();
    final String packageFullName = getFullPackageName(packageScope, packageName);
    final boolean included = filteringService.included(proxyRegistry,
                                                       packageFullName);
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    final String cacheKey = String.format("%s/%s", packageFullName, fileName);
    if (useCache(context, cacheKey)) {
      return downloadTarballLocal(context,
                                  packageScope,
                                  packageName,
                                  fileName);
    }

    boolean askRemote = false;
    Path tarballPath = null;
    if (storageHelper.getDoStore(context.getRegistry())) {
      tarballPath = getPackageStorage().getTarballFilePath(context.getRegistry(),
                                                           packageScope,
                                                           packageName,
                                                           fileName);
    }

    if (tarballPath == null) {
      askRemote = true;
    } else {
      final String packageVersion = extractVersionFromFileName(packageName, fileName);
      final Version localVersion = getPackageStorage().getPackageVersion(context.getRegistry(),
                                                                         getRegistryUri(request),
                                                                         packageScope,
                                                                         packageName,
                                                                         packageVersion);
      if (localVersion == null) {
        storeRemotePackage(context,
                           request,
                           packageScope,
                           packageName);

        askRemote = true;
      } else {
        Version remoteVersion = getPackageVersionRemote(context,
                                                        packageScope,
                                                        packageName,
                                                        packageVersion);
        if (remoteVersion == null) {
          remoteVersion = getPackageVersionFromPackageJson(context,
                                                           request,
                                                           packageScope,
                                                           packageName,
                                                           packageVersion);
        }

        if (remoteVersion == null) {
          askRemote = true;
        } else {
          final String localIntegrity = localVersion.getDist().getIntegrity();
          final String remoteIntegrity = remoteVersion.getDist().getIntegrity();
          askRemote = !localIntegrity.equals(remoteIntegrity);
        }
      }
    }

    if (askRemote) {
      return downloadTarballRemote(context,
                                   request,
                                   packageScope,
                                   packageName,
                                   fileName);
    }

    return downloadTarballLocal(tarballPath);
  }

  private <T> T getPackageRemote(final NpmOperationContext context,
                                 final String packageScope,
                                 final String packageName,
                                 final String checksum,
                                 final CheckedFunction<ClassicHttpResponse, T> proxyResponseNotModifiedFunction,
                                 final CheckedFunction<ClassicHttpResponse, T> proxyResponseErrorFunction,
                                 final CheckedFunction<ClassicHttpResponse, T> proxyResponseChecksumCheckerFunction,
                                 final CheckedBiFunction<CloseableHttpClient, ClassicHttpResponse, T> proxyResponseOkFunction)
    throws Exception {
    final NpmProxyRegistry proxyRegistry = (NpmProxyRegistry) context.getRegistry();

    final String packageFullName = getFullPackageName(packageScope, packageName);
    final URIBuilder uriBuilder =
      new URIBuilder(String.format(GET_PACKAGE_URI,
                                   proxyRegistry.getProxyUrl(),
                                   packageFullName));

    final URI uri = uriBuilder.build();
    final HttpGet httpGet = new HttpGet(uri);
    proxyAuthenticationManager.addAuthentication(httpGet,
                                                 proxyRegistry);
    if (checksum != null) {
      httpGet.addHeader(HttpHeaders.IF_NONE_MATCH, checksum);
    }

    boolean doClose = true;
    final CloseableHttpClient httpClient = httpHelper.createHttpClient();
    try {
      final ClassicHttpResponse proxyResponse = httpClient.executeOpen(null, httpGet, null);
      try {
        if (proxyResponse.getCode() == org.apache.hc.core5.http.HttpStatus.SC_NOT_MODIFIED) {
          return proxyResponseNotModifiedFunction.apply(proxyResponse);
        }

        if (proxyResponse.getCode() != org.apache.hc.core5.http.HttpStatus.SC_OK) {
          final Registry registry = context.getRegistry();
          LOGGER.error("[{}] Proxy answered with code {} when getting package {}",
                       registry.getName(),
                       proxyResponse.getCode(),
                       packageFullName);
          return proxyResponseErrorFunction.apply(proxyResponse);
        }

        if (proxyResponseChecksumCheckerFunction != null) {
          final T result = proxyResponseChecksumCheckerFunction.apply(proxyResponse);
          if (result != null) {
            return result;
          }
        }

        doClose = false;
        return proxyResponseOkFunction.apply(httpClient, proxyResponse);
      } finally {
        if (doClose) {
          try {
            proxyResponse.close();
          } catch (final IOException exception) {
            LOGGER.error(exception.getMessage(), exception);
          }
        }
      }
    } finally {
      if (doClose) {
        try {
          httpClient.close();
        } catch (final IOException exception) {
          LOGGER.error(exception.getMessage(), exception);
        }
      }
    }
  }

  private void storeRemotePackage(final NpmOperationContext context,
                                  final HttpServletRequest request,
                                  final String packageScope,
                                  final String packageName)
    throws Exception {
    final CheckedFunction<ClassicHttpResponse, Void> notModifiedFunction =
      proxyResponse -> {
        return null;
      };

    final CheckedFunction<ClassicHttpResponse, Void> errorFunction =
      proxyResponse -> {
        return null;
      };

    final CheckedBiFunction<CloseableHttpClient, ClassicHttpResponse, Void> okFunction =
      (httpClient, proxyResponse) -> {
        storePackageJson(context,
                         request,
                         packageScope,
                         packageName,
                         httpClient,
                         proxyResponse);
        return null;
      };

    getPackageRemote(context,
                     packageScope,
                     packageName,
                     null,
                     notModifiedFunction,
                     errorFunction,
                     notModifiedFunction,
                     okFunction);
  }

  private GetPackageResult storePackageJson(final NpmOperationContext context,
                                            final HttpServletRequest request,
                                            final String packageScope,
                                            final String packageName,
                                            final CloseableHttpClient httpClient,
                                            final ClassicHttpResponse proxyResponse)
    throws Exception {
    try {
      final String etag = getEtag(proxyResponse);
      final HttpEntity entity = proxyResponse.getEntity();
      final InputStream inputStream = entity.getContent();
      final ResponsePackage responsePackage =
        getObjectMapper().readValue(inputStream, ResponsePackage.class);

      if (storageHelper.getDoStore(context.getRegistry())) {
        return getPackageStorage().createPackageJson(context.getRegistry(),
                                                     getRegistryUri(request),
                                                     packageScope,
                                                     packageName,
                                                     responsePackage,
                                                     etag);
      } else {
        getPackageStorage().fixResponsePackage(getRegistryUri(request),
                                               packageScope,
                                               packageName,
                                               responsePackage);
        return GetPackageResult.builder()
          .responsePackage(responsePackage)
          .checksum(etag)
          .build();
      }
    } finally {
      closeSilently(proxyResponse);
      closeSilently(httpClient);
    }
  }

  private Version getPackageVersionFromPackageJson(final NpmOperationContext context,
                                                   final HttpServletRequest request,
                                                   final String packageScope,
                                                   final String packageName,
                                                   final String packageVersion)
    throws Exception {
    final String checksum;
    if (storageHelper.getDoStore(context.getRegistry())) {
      checksum = getPackageStorage().getPackageChecksum(context.getRegistry(),
                                                        packageScope,
                                                        packageName);
    } else {
      checksum = null;
    }

    final CheckedFunction<ClassicHttpResponse, Version> notModifiedFunction =
      proxyResponse -> {
        return getPackageStorage().getPackageVersion(context.getRegistry(),
                                                     getRegistryUri(request),
                                                     packageScope,
                                                     packageName,
                                                     packageVersion);
      };

    final CheckedFunction<ClassicHttpResponse, Version> errorFunction =
      proxyResponse -> {
        return null;
      };

    final CheckedBiFunction<CloseableHttpClient, ClassicHttpResponse, Version> okFunction =
      (httpClient, proxyResponse) -> {
        storePackageJson(context,
                         request,
                         packageScope,
                         packageName,
                         httpClient,
                         proxyResponse);

        return getPackageStorage().getPackageVersion(context.getRegistry(),
                                                     getRegistryUri(request),
                                                     packageScope,
                                                     packageName,
                                                     packageVersion);

      };

    return getPackageRemote(context,
                            packageScope,
                            packageName,
                            checksum,
                            notModifiedFunction,
                            errorFunction,
                            notModifiedFunction,
                            okFunction);
  }

  private Version getPackageVersionRemote(final NpmOperationContext context,
                                          final String packageScope,
                                          final String packageName,
                                          final String packageVersion)
    throws Exception {
    final NpmProxyRegistry proxyRegistry = (NpmProxyRegistry) context.getRegistry();

    final String packageFullName = getFullPackageName(packageScope, packageName);
    final URIBuilder uriBuilder =
      new URIBuilder(String.format(GET_PACKAGE_VERSION_URI,
                                   proxyRegistry.getProxyUrl(),
                                   packageFullName,
                                   packageVersion));

    final URI uri = uriBuilder.build();
    final HttpGet httpGet = new HttpGet(uri);
    proxyAuthenticationManager.addAuthentication(httpGet,
                                                 proxyRegistry);

    try (CloseableHttpClient httpClient = httpHelper.createHttpClient()) {
      return httpClient.execute(httpGet, response -> {
        if (response.getCode() != org.apache.hc.core5.http.HttpStatus.SC_OK) {
          return null;
        }
        final HttpEntity entity = response.getEntity();
        return getObjectMapper().readValue(entity.getContent(), Version.class);
      });
    }
  }

  private ResponseEntity<StreamingResponseBody> downloadTarballRemote(final NpmOperationContext context,
                                                                      final HttpServletRequest request,
                                                                      final String packageScope,
                                                                      final String packageName,
                                                                      final String fileName)
    throws Exception {
    final NpmProxyRegistry proxyRegistry = (NpmProxyRegistry) context.getRegistry();

    final String packageFullName = getFullPackageName(packageScope, packageName);
    final URIBuilder uriBuilder =
      new URIBuilder(String.format(DOWNLOAD_TARBALL_URI,
                                   proxyRegistry.getProxyUrl(),
                                   packageFullName,
                                   fileName));

    final URI uri = uriBuilder.build();

    final HttpGet httpGet = new HttpGet(uri);
    proxyAuthenticationManager.addAuthentication(httpGet,
                                                 proxyRegistry);

    boolean doClose = true;
    final CloseableHttpClient httpClient = httpHelper.createHttpClient();
    try {
      final ClassicHttpResponse proxyResponse = httpClient.executeOpen(null, httpGet, null);
      try {
        if (proxyResponse.getCode() != org.apache.hc.core5.http.HttpStatus.SC_OK) {
          final Registry registry = context.getRegistry();
          LOGGER.error("[{}] Proxy answered with code {} when downloading tarball {}/{}",
                       registry.getName(),
                       proxyResponse.getCode(),
                       packageFullName,
                       fileName);
          return ResponseEntity.status(HttpStatus.valueOf(proxyResponse.getCode())).build();
        }

        final byte[] buffer = new byte[10240];
        final HttpEntity entity = proxyResponse.getEntity();
        final InputStream inputStream = entity.getContent();

        final boolean doStore = storageHelper.getDoStore(context.getRegistry());
        if (doStore) {
          // FIXME: for performance reasons we stream to the client the same time we store
          // locally
          // so we create elements in database before they actually exist on drive
          getPackageStorage().prepareTarballCreation(context.getRegistry(),
                                                     packageScope,
                                                     packageName,
                                                     fileName);
        }

        final CloseableHolder clientCloseableHolder = new CloseableHolder(httpClient);
        closeableCleaner.add(clientCloseableHolder);
        final CloseableHolder responseCloseableHolder = new CloseableHolder(proxyResponse);
        closeableCleaner.add(responseCloseableHolder);

        doClose = false;
        final StreamingResponseBody stream = outputStream -> {
          try {
            TarballCreation tarballCreation = null;
            if (doStore) {
              tarballCreation = getPackageStorage().initiateTarballCreation(context.getRegistry(),
                                                                            packageScope,
                                                                            packageName,
                                                                            fileName);
            }
            try {
              int read;
              while ((read = inputStream.read(buffer)) != -1) {
                clientCloseableHolder.setLastUsed(System.currentTimeMillis());
                responseCloseableHolder.setLastUsed(System.currentTimeMillis());
                if (tarballCreation != null) {
                  tarballCreation.getOutputStream().write(buffer, 0, read);
                }
                outputStream.write(buffer, 0, read);
              }

              if (doStore) {
                getPackageStorage().createTarball(context.getRegistry(), tarballCreation);
              }
            } catch (final Throwable throwable) {
              if (doStore) {
                getPackageStorage().abortTarballCreation(context.getRegistry(), tarballCreation);
              }
              throw throwable;
            }
          } finally {
            proxyResponse.close();
            httpClient.close();
            closeableCleaner.remove(clientCloseableHolder);
            closeableCleaner.remove(responseCloseableHolder);
          }
        };

        final HttpHeaders headers = createHeadersWithEtag(proxyResponse);

        return ResponseEntity.status(HttpStatus.OK)
          .headers(headers)
          .body(stream);
      } finally {
        if (doClose) {
          try {
            proxyResponse.close();
          } catch (final IOException exception) {
            LOGGER.error(exception.getMessage(), exception);
          }
        }
      }
    } finally {
      if (doClose) {
        try {
          httpClient.close();
        } catch (final IOException exception) {
          LOGGER.error(exception.getMessage(), exception);
        }
      }
    }
  }

  private HttpHeaders createHeadersWithEtag(final ClassicHttpResponse response) {
    final String etag = getEtag(response);
    return createHeadersWithEtag(etag);
  }

  private String getEtag(final ClassicHttpResponse response) {
    final Header etagHeader = response.getFirstHeader(HttpHeaders.ETAG);
    if (etagHeader == null) {
      return null;
    }
    final String etagAsString = etagHeader.getValue();
    final ETag etag = ETag.create(etagAsString);
    return etag.tag();
  }

  private HttpHeaders createHeadersWithEtag(final String etag) {
    final HttpHeaders headers = new HttpHeaders();
    if (etag != null) {
      headers.add(HttpHeaders.ETAG, etag);
    }
    return headers;
  }

  private boolean useCache(final NpmOperationContext context,
                           final String cacheKey) {
    final Registry registry = context.getRegistry();
    return proxyCache.upToDate(registry.getName(), cacheKey);
  }
}
