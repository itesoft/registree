package com.itesoft.registree.raw.rest.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.CloseableCleaner;
import com.itesoft.registree.CloseableHolder;
import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.Registry;
import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.raw.dto.RawFile;
import com.itesoft.registree.raw.dto.RawFileCreation;
import com.itesoft.registree.raw.rest.RawFileManager;
import com.itesoft.registree.raw.rest.RawOperationContext;
import com.itesoft.registree.raw.rest.ReadOnlyRawFileManager;
import com.itesoft.registree.raw.storage.FileStorage;
import com.itesoft.registree.registry.api.storage.StorageHelper;
import com.itesoft.registree.registry.filtering.ProxyFilteringService;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class ProxyFileManager extends ReadOnlyRawFileManager implements RawFileManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyFileManager.class);

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private FileStorage fileStorage;

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
  public ResponseEntity<StreamingResponseBody> getFile(final RawOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String path)
    throws Exception {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) context.getRegistry();
    final String normalizePath = fileStorage.normalizePath(path);
    final boolean included = filteringService.included(proxyRegistry,
                                                       normalizePath);
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    RawFile file = null;
    if (storageHelper.getDoStore(context.getRegistry())) {
      file = fileStorage.getFile(context.getRegistry(),
                                 path);
    }

    if (file == null) {
      return downloadFileRemote(context,
                                request,
                                path);
    }

    return getFileLocal(file);
  }

  private ResponseEntity<StreamingResponseBody> downloadFileRemote(final RawOperationContext context,
                                                                   final HttpServletRequest request,
                                                                   final String name)
    throws Exception {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) context.getRegistry();

    final URIBuilder uriBuilder =
      new URIBuilder(String.format("%s%s",
                                   proxyRegistry.getProxyUrl(),
                                   name));

    final URI uri = uriBuilder.build();
    final HttpGet httpGet = new HttpGet(uri);
    boolean doClose = true;
    final ClassicHttpResponse proxyResponse = httpClient.executeOpen(null, httpGet, null);
    try {
      if (proxyResponse.getCode() != org.apache.hc.core5.http.HttpStatus.SC_OK) {
        final Registry registry = context.getRegistry();
        LOGGER.error("[{}] Proxy answered with code {} when downloading file {}",
                     registry.getName(),
                     proxyResponse.getCode(),
                     name);
        return ResponseEntity.status(HttpStatus.valueOf(proxyResponse.getCode())).build();
      }

      final CloseableHolder responseCloseableHolder = new CloseableHolder(proxyResponse);
      closeableCleaner.add(responseCloseableHolder);

      final byte[] buffer = new byte[10240];
      final HttpEntity entity = proxyResponse.getEntity();
      final String contentType = entity.getContentType();
      final InputStream inputStream = entity.getContent();

      final boolean doStore = storageHelper.getDoStore(context.getRegistry());
      if (doStore) {
        // FIXME: for performance reasons we stream to the client the same time we store
        // locally
        // so we create elements in database before they actually exist on drive
        fileStorage.prepareFileCreation(context.getRegistry(),
                                        name,
                                        contentType);
      }

      doClose = false;
      final StreamingResponseBody stream = outputStream -> {
        try {
          RawFileCreation rawFileCreation = null;
          if (doStore) {
            rawFileCreation = fileStorage.initiateFileCreation(context.getRegistry(),
                                                               name);
          }
          try {
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
              responseCloseableHolder.setLastUsed(System.currentTimeMillis());
              if (rawFileCreation != null) {
                rawFileCreation.getOutputStream().write(buffer, 0, read);
              }
              outputStream.write(buffer, 0, read);
            }

            if (doStore) {
              fileStorage.createFile(context.getRegistry(), rawFileCreation);
            }
          } catch (final Throwable throwable) {
            if (doStore) {
              fileStorage.abortFileCreation(context.getRegistry(), rawFileCreation);
            }
            throw throwable;
          }
        } finally {
          proxyResponse.close();
          closeableCleaner.remove(responseCloseableHolder);
        }
      };

      return ResponseEntity.status(HttpStatus.OK)
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
  }
}
