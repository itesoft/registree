package com.itesoft.registree.oci.rest.proxy;

import static com.itesoft.registree.oci.rest.error.ErrorCode.BLOB_UNKNOWN;
import static com.itesoft.registree.oci.rest.proxy.ProxyHelper.getRemoteName;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.CloseableCleaner;
import com.itesoft.registree.CloseableHolder;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.oci.config.OciProxyRegistry;
import com.itesoft.registree.oci.dto.Blob;
import com.itesoft.registree.oci.dto.BlobUpload;
import com.itesoft.registree.oci.rest.OciOperationContext;
import com.itesoft.registree.oci.rest.OciRegistryBlobManager;
import com.itesoft.registree.oci.rest.ReadOnlyRegistryBlobManager;
import com.itesoft.registree.oci.rest.error.OciErrorManager;
import com.itesoft.registree.oci.rest.proxy.auth.OciProxyAuthenticationManager;
import com.itesoft.registree.oci.storage.BlobStorage;
import com.itesoft.registree.oci.storage.RepositoryStorage;
import com.itesoft.registree.registry.api.storage.StorageHelper;
import com.itesoft.registree.registry.filtering.ProxyFilteringService;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
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
public class ProxyRegistryBlobManager extends ReadOnlyRegistryBlobManager implements OciRegistryBlobManager {
  private static final String GET_BLOB_URL = "%s/v2/%s/blobs/%s";

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyRegistryBlobManager.class);

  @Autowired
  private OciErrorManager errorManager;

  @Autowired
  private OciProxyAuthenticationManager proxyAuthenticationManager;

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private BlobStorage blobStorage;

  @Autowired
  private RepositoryStorage repositoryStorage;

  @Autowired
  private ProxyFilteringService filteringService;

  @Autowired
  private CloseableCleaner closeableCleaner;

  @Autowired
  private HttpClient httpClient;

  @Override
  public RegistryType getType() {
    return RegistryType.PROXY;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> blobExists(final OciOperationContext context,
                                                          final HttpServletRequest request,
                                                          final String name,
                                                          final String digest)
    throws Exception {
    return getBlob(context,
                   request,
                   uri -> new HttpHead(uri),
                   name,
                   digest,
                   false);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getBlob(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String digest)
    throws Exception {
    final Blob blob = blobStorage.getBlob(context.getRegistry(),
                                          digest);
    if (blob == null) {
      return errorManager.getErrorResponse(HttpStatus.NOT_FOUND,
                                           BLOB_UNKNOWN,
                                           String.format("Cannot find blob %s", digest));
    }
    return getBlobResponse(blob, true);
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getBlob(final OciOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String name,
                                                       final String digest)
    throws Exception {
    return getBlob(context,
                   request,
                   uri -> new HttpGet(uri),
                   name,
                   digest,
                   true);
  }

  private ResponseEntity<StreamingResponseBody> getBlob(final OciOperationContext context,
                                                        final HttpServletRequest request,
                                                        final Function<URI, HttpUriRequestBase> proxyRequestSupplier,
                                                        final String name,
                                                        final String digest,
                                                        final boolean withData)
    throws Exception {
    final OciProxyRegistry proxyRegistry = (OciProxyRegistry) context.getRegistry();
    final boolean included = filteringService.included(proxyRegistry,
                                                       name);
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    final Blob blob;
    if (storageHelper.getDoStore(context.getRegistry())) {
      blob = getBlob(context, name, digest);
    } else {
      blob = null;
    }

    if (blob != null) {
      return getBlobResponse(blob, withData);
    }
    return getBlobRemote(context,
                         request,
                         proxyRequestSupplier,
                         name,
                         digest);
  }

  private ResponseEntity<StreamingResponseBody> getBlobRemote(final OciOperationContext context,
                                                              final HttpServletRequest request,
                                                              final Function<URI, HttpUriRequestBase> proxyRequestSupplier,
                                                              final String name,
                                                              final String digest)
    throws Exception {
    // TODO: add some ping to remove host, if not ok, return not found fast

    final String remoteName = getRemoteName(name);

    final OciProxyRegistry proxyRegistry = (OciProxyRegistry) context.getRegistry();

    final URIBuilder uriBuilder =
      new URIBuilder(String.format(GET_BLOB_URL,
                                   proxyRegistry.getProxyUrl(),
                                   remoteName,
                                   digest));
    for (final Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
      uriBuilder.addParameter(entry.getKey(), entry.getValue()[0]);
    }
    final URI uri = uriBuilder.build();

    HttpEntity entity = null;
    final ClassicHttpRequest proxyRequest = proxyRequestSupplier.apply(uri);
    proxyRequest.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip");
    final boolean authenticated =
      proxyAuthenticationManager.addAuthentication(proxyRequest,
                                                   proxyRegistry,
                                                   remoteName);
    if (!authenticated) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .build();
    }

    final ClassicHttpResponse proxyResponse = httpClient.executeOpen(null, proxyRequest, null);
    try {
      if (proxyResponse.getCode() != org.apache.hc.core5.http.HttpStatus.SC_OK) {
        LOGGER.error("[{}] Proxy answered with code {} when getting blob of {}@{}",
                     proxyRegistry.getName(),
                     proxyResponse.getCode(),
                     name,
                     digest);
        return ResponseEntity.status(HttpStatus.valueOf(proxyResponse.getCode())).build();
      }

      final Header contentTypeHeader = proxyResponse.getHeader(HttpHeaders.CONTENT_TYPE);
      if (contentTypeHeader == null) {
        // TODO: find better error code?
        LOGGER.error("[{}] Failed to get content type from proxy",
                     proxyRegistry.getName());
        return errorManager.getErrorResponse(HttpStatus.BAD_REQUEST,
                                             BLOB_UNKNOWN,
                                             "Failed to get content type from proxy");
      }
      final Header contentLengthHeader = proxyResponse.getHeader(HttpHeaders.CONTENT_LENGTH);
      if (contentLengthHeader == null) {
        // TODO: find better error code?
        LOGGER.error("[{}] Failed to get content length from proxy",
                     proxyRegistry.getName());
        return errorManager.getErrorResponse(HttpStatus.BAD_REQUEST,
                                             BLOB_UNKNOWN,
                                             "Failed to get content length from proxy");
      }

      final HttpHeaders headers = getGetBlobHeaders(contentTypeHeader.getValue(),
                                                    contentLengthHeader.getValue(),
                                                    digest);

      final OutputStream blobStream;
      final BlobUpload blobUpload;
      if (storageHelper.getDoStore(context.getRegistry())) {
        blobUpload = blobStorage.createBlobUpload(context.getRegistry(), name);
        blobStream = blobUpload.getOutputStream();
      } else {
        blobUpload = null;
        blobStream = null;
      }

      StreamingResponseBody stream = null;
      entity = proxyResponse.getEntity();
      if (entity != null) {
        final CloseableHolder responseCloseableHolder = new CloseableHolder(proxyResponse);
        closeableCleaner.add(responseCloseableHolder);

        final byte[] buffer = new byte[10240];
        final InputStream inputStream = entity.getContent();

        stream = outputStream -> {
          try {
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
              responseCloseableHolder.setLastUsed(System.currentTimeMillis());
              if (blobStream != null) {
                blobStream.write(buffer, 0, read);
              }
              outputStream.write(buffer, 0, read);
            }
          } finally {
            proxyResponse.close();
            closeableCleaner.remove(responseCloseableHolder);
          }

          if (blobUpload != null) {
            // TODO: createBlobFromUpload triggers createFile and sets anonymous user,
            // no authentication available here since with are in a StreamingResponseBody
            // performed from another thread
            blobStorage.createBlobFromUpload(context.getRegistry(), name, digest, blobUpload.getUuid());
            repositoryStorage.createLayer(context.getRegistry(), name, digest);
          }
        };
      }

      return ResponseEntity.ok().headers(headers).body(stream);
    } finally {
      if (entity == null) {
        try {
          proxyResponse.close();
        } catch (final IOException exception) {
          LOGGER.error(exception.getMessage(), exception);
        }
      }
    }
  }
}
