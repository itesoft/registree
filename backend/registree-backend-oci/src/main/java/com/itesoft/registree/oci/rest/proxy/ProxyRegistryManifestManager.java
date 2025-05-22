package com.itesoft.registree.oci.rest.proxy;

import static com.itesoft.registree.oci.rest.error.ErrorCode.MANIFEST_INVALID;
import static com.itesoft.registree.oci.rest.proxy.ProxyHelper.getRemoteName;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.java.CheckedFunction;
import com.itesoft.registree.oci.config.OciProxyRegistry;
import com.itesoft.registree.oci.dto.Manifest;
import com.itesoft.registree.oci.dto.json.ManifestDto;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.OciRegistryManifestManager;
import com.itesoft.registree.oci.rest.ReadOnlyRegistryManifestManager;
import com.itesoft.registree.oci.rest.error.OciErrorManager;
import com.itesoft.registree.oci.rest.proxy.auth.OciProxyAuthenticationManager;
import com.itesoft.registree.oci.storage.RepositoryStorage;
import com.itesoft.registree.proxy.HttpHelper;
import com.itesoft.registree.registry.api.storage.StorageHelper;
import com.itesoft.registree.registry.filtering.ProxyFilteringService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class ProxyRegistryManifestManager extends ReadOnlyRegistryManifestManager implements OciRegistryManifestManager {
  private static final String GET_MANIFEST_URL = "%s/v2/%s/manifests/%s";

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRegistryManifestManager.class);

  @Autowired
  private OciErrorManager errorManager;

  @Autowired
  private OciProxyAuthenticationManager proxyAuthenticationManager;

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private RepositoryStorage repositoryStorage;

  @Autowired
  private ProxyFilteringService filteringService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpHelper httpHelper;

  @Override
  public RegistryType getType() {
    return RegistryType.PROXY;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> manifestExists(final OciOperationContext context,
                                                              final HttpServletRequest request,
                                                              final String name,
                                                              final String reference)
    throws Exception {
    return getManifest(context,
                       request,
                       uri -> new HttpHead(uri),
                       name,
                       reference,
                       false);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getManifest(final OciOperationContext context,
                                                           final HttpServletRequest request,
                                                           final String name,
                                                           final String reference)
    throws Exception {
    return getManifest(context,
                       request,
                       uri -> new HttpGet(uri),
                       name,
                       reference,
                       true);
  }

  private ResponseEntity<StreamingResponseBody> getManifest(final OciOperationContext context,
                                                            final HttpServletRequest request,
                                                            final Function<URI, HttpUriRequestBase> proxyRequestSupplier,
                                                            final String name,
                                                            final String reference,
                                                            final boolean withData)
    throws Exception {
    final OciProxyRegistry proxyRegistry = (OciProxyRegistry) context.getRegistry();
    final boolean included = filteringService.included(proxyRegistry,
                                                       name);
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    final Manifest manifest;
    if (storageHelper.getDoStore(context.getRegistry())) {
      manifest = getManifest(context, name, reference, withData);
    } else {
      manifest = null;
    }

    if (manifest != null) {
      final Header remoteDigestHeader =
        getManifestRemote(context,
                          request,
                          proxyRequestSupplier,
                          proxyResponse -> proxyResponse.getHeader("Docker-Content-Digest"),
                          name,
                          reference);
      if (remoteDigestHeader == null) { // unauthorized, use local data
        return getManifestResponse(manifest, withData);
      }

      final String remoteDigest = remoteDigestHeader.getValue();
      if (manifest.getDigest().equals(remoteDigest)) {
        return getManifestResponse(manifest, withData);
      }
    }

    return getManifestRemote(context, request, proxyRequestSupplier, name, reference, withData);
  }

  private ResponseEntity<StreamingResponseBody> getManifestRemote(final OciOperationContext context,
                                                                  final HttpServletRequest request,
                                                                  final Function<URI, HttpUriRequestBase> proxyRequestSupplier,
                                                                  final String name,
                                                                  final String reference,
                                                                  final boolean withData)
    throws Exception {
    final OciProxyRegistry proxyRegistry = (OciProxyRegistry) context.getRegistry();

    final CheckedFunction<ClassicHttpResponse, ResponseEntity<StreamingResponseBody>> proxyResponseManager = (proxyResponse) -> {
      final HttpEntity entity = proxyResponse.getEntity();
      if (proxyResponse.getCode() != org.apache.hc.core5.http.HttpStatus.SC_OK) {
        LOGGER.error("[{}] Proxy answered with code {} when getting manifest of {}%{}",
                     proxyRegistry.getName(),
                     proxyResponse.getCode(),
                     name,
                     reference);
        return ResponseEntity.status(HttpStatus.valueOf(proxyResponse.getCode())).build();
      }

      final Header contentTypeHeader = proxyResponse.getHeader(HttpHeaders.CONTENT_TYPE);
      if (contentTypeHeader == null) {
        LOGGER.error("[{}] Failed to get content type from proxy for {}@{}",
                     proxyRegistry.getName(),
                     name,
                     reference);
        return errorManager.getErrorResponse(HttpStatus.BAD_REQUEST,
                                             MANIFEST_INVALID,
                                             "Failed to get content type from proxy");
      }
      final Header contentLengthHeader = proxyResponse.getHeader(HttpHeaders.CONTENT_LENGTH);
      if (contentLengthHeader == null) {
        LOGGER.error("[{}] Failed to get content length from proxy for {}@{}",
                     proxyRegistry.getName(),
                     name,
                     reference);
        return errorManager.getErrorResponse(HttpStatus.BAD_REQUEST,
                                             MANIFEST_INVALID,
                                             "Failed to get content length from proxy");
      }
      final Header digestHeader = proxyResponse.getHeader("Docker-Content-Digest");
      if (digestHeader == null) {
        LOGGER.error("[{}] Failed to get digest from proxy for {}@{}",
                     proxyRegistry.getName(),
                     name,
                     reference);
        return errorManager.getErrorResponse(HttpStatus.BAD_REQUEST,
                                             MANIFEST_INVALID,
                                             "Failed to get digest from proxy");
      }
      final String digest = digestHeader.getValue();

      final HttpHeaders headers = getGetManifestHeaders(contentTypeHeader.getValue(),
                                                        contentLengthHeader.getValue(),
                                                        digest);

      final byte[] manifest = EntityUtils.toByteArray(entity);
      final ManifestDto manifestDto = objectMapper.readValue(manifest, ManifestDto.class);
      if (manifestDto.getSchemaVersion() != 2) {
        LOGGER.warn("[{}] Downloaded manifest with version {} for {}@{}, this may cause trouble",
                    proxyRegistry.getName(),
                    manifestDto.getSchemaVersion(),
                    name,
                    reference);
      }

      if (storageHelper.getDoStore(context.getRegistry())) {
        final String tag = reference.equals(digest) ? null : reference;
        repositoryStorage.createManifest(context.getRegistry(), name, tag, digest, manifestDto.getMediaType(), manifest);
      }

      final StreamingResponseBody stream;
      if (withData) {
        stream = outputStream -> {
          outputStream.write(manifest);
        };
      } else {
        stream = null;
      }

      return ResponseEntity.ok().headers(headers).body(stream);
    };
    final ResponseEntity<StreamingResponseBody> result =
      getManifestRemote(context,
                        request,
                        uri -> new HttpGet(uri),
                        proxyResponseManager,
                        name,
                        reference);
    if (result == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .build();
    }
    return result;
  }

  private <T> T getManifestRemote(final OciOperationContext context,
                                  final HttpServletRequest request,
                                  final Function<URI, HttpUriRequestBase> proxyRequestSupplier,
                                  final CheckedFunction<ClassicHttpResponse, T> proxyResponseManager,
                                  final String name,
                                  final String reference)
    throws Exception {
    // TODO: add some ping to remove host, if not ok, return not found fast

    final String remoteName = getRemoteName(name);

    final OciProxyRegistry proxyRegistry = (OciProxyRegistry) context.getRegistry();

    final URIBuilder uriBuilder =
      new URIBuilder(String.format(GET_MANIFEST_URL,
                                   proxyRegistry.getProxyUrl(),
                                   remoteName,
                                   reference));
    for (final Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
      uriBuilder.addParameter(entry.getKey(), entry.getValue()[0]);
    }
    final URI uri = uriBuilder.build();

    try (CloseableHttpClient httpClient = httpHelper.createHttpClient()) {
      final ClassicHttpRequest proxyRequest = proxyRequestSupplier.apply(uri);
      addAcceptHeader(proxyRequest);
      final boolean authenticated =
        proxyAuthenticationManager.addAuthentication(proxyRequest,
                                                     proxyRegistry,
                                                     remoteName);
      if (!authenticated) {
        return null;
      }

      return httpClient.execute(proxyRequest, proxyResponse -> {
        try {
          return proxyResponseManager.apply(proxyResponse);
        } catch (final IOException exception) {
          throw exception;
        } catch (final Exception exception) {
          throw new IOException(exception.getMessage(), exception);
        }
      });
    }
  }

  private void addAcceptHeader(final ClassicHttpRequest request) {
    final Header acceptheader = new BasicHeader(HttpHeaders.ACCEPT,
                                                "\"application/vnd.docker.distribution.manifest.v2+json\","
                                                  + "\"application/vnd.docker.distribution.manifest.list.v2+json\","
                                                  + "\"application/vnd.oci.image.index.v1+json\","
                                                  + "\"application/vnd.oci.image.manifest.v1+json\"");

    request.addHeader(acceptheader);
  }
}
