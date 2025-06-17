package com.itesoft.registree.maven.rest.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.itesoft.registree.CloseableCleaner;
import com.itesoft.registree.CloseableHolder;
import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.java.CheckedConsumer;
import com.itesoft.registree.java.CheckedRunnable;
import com.itesoft.registree.java.CheckedSupplier;
import com.itesoft.registree.maven.dto.FileCreation;
import com.itesoft.registree.maven.dto.MavenFile;
import com.itesoft.registree.maven.rest.MavenOperationContext;
import com.itesoft.registree.proxy.ProxyCache;
import com.itesoft.registree.registry.api.storage.StorageHelper;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class ProxyHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProxyHelper.class);

  @Autowired
  private StorageHelper storageHelper;

  @Autowired
  private CloseableCleaner closeableCleaner;

  @Autowired
  private HttpClient httpClient;

  @Autowired
  private ProxyCache proxyCache;

  public static String toPath(final String groupPath,
                              final String artifactId) {
    return toPath(groupPath, artifactId, null);
  }

  public static String toPath(final String groupPath,
                              final String artifactId,
                              final String version) {
    if (version == null) {
      return String.format("%s/%s",
                           groupPath,
                           artifactId);
    } else {
      return String.format("%s/%s/%s",
                           groupPath,
                           artifactId,
                           version);
    }

  }

  public ResponseEntity<StreamingResponseBody> fileExists(final MavenOperationContext context,
                                                          final CheckedSupplier<Boolean, Exception> includedSupplier,
                                                          final Supplier<String> cacheKeySupplier,
                                                          final CheckedSupplier<URI, Exception> fileUriSupplier,
                                                          final CheckedSupplier<MavenFile, Exception> localFileSupplier)
    throws Exception {
    final boolean included = includedSupplier.get();
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    final MavenFile file = localFileSupplier.get();
    boolean existsRemote = false;
    if (file == null && !useCache(context, cacheKeySupplier)) {
      final URI fileUri = fileUriSupplier.get();
      final HttpHead httpHead = new HttpHead(fileUri);
      existsRemote =
        httpClient.execute(httpHead, (response) -> {
          return response.getCode() == HttpStatus.OK.value();
        });
    }

    if (file != null || existsRemote) {
      return ResponseEntity.ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  public <T extends FileCreation> ResponseEntity<StreamingResponseBody> getFile(final MavenOperationContext context,
                                                                                final CheckedSupplier<Boolean, Exception> includedSupplier,
                                                                                final Supplier<String> cacheKeySupplier,
                                                                                final CheckedSupplier<URI, Exception> fileUriSupplier,
                                                                                final CheckedSupplier<URI, Exception> md5UriSupplier,
                                                                                final CheckedSupplier<String, Exception> md5Supplier,
                                                                                final CheckedConsumer<String, Exception> publishMd5Consumer,
                                                                                final Consumer<ClassicHttpResponse> proxyErrorMessageConsumer,
                                                                                final CheckedRunnable prepareCreationRunnable,
                                                                                final CheckedSupplier<T, Exception> initiateCreationSupplier,
                                                                                final CheckedConsumer<T, Exception> createFileConsumer,
                                                                                final Consumer<T> abortFileCreationConsumer,
                                                                                final CheckedSupplier<ResponseEntity<StreamingResponseBody>, Exception> getlfs)
    throws Exception {
    final CheckedSupplier<ResponseEntity<StreamingResponseBody>, Exception> getLocalFileSupplier = getlfs;
    final boolean included = includedSupplier.get();
    if (!included) {
      return ResponseEntity.notFound().build();
    }

    if (useCache(context, cacheKeySupplier)) {
      return getLocalFileSupplier.get();
    }

    final String md5;
    if (storageHelper.getDoStore(context.getRegistry())) {
      md5 = md5Supplier.get();
    } else {
      md5 = null;
    }

    boolean askRemote = true;
    boolean doClose = true;
    if (md5 != null) {
      final URI md5Uri = md5UriSupplier.get();
      final HttpGet httpGet = new HttpGet(md5Uri);
      final String remoteMd5 =
        httpClient.execute(httpGet, (response) -> {
          if (response.getCode() != HttpStatus.OK.value()) {
            return null;
          }
          return EntityUtils.toString(response.getEntity());
        });

      if (md5.equals(remoteMd5)) {
        askRemote = false;
      } else {
        publishMd5Consumer.accept(remoteMd5);
      }
    }

    if (askRemote) {
      final URI fileUri = fileUriSupplier.get();
      final HttpGet httpGet = new HttpGet(fileUri);
      final ClassicHttpResponse proxyResponse = httpClient.executeOpen(null, httpGet, null);
      try {
        if (proxyResponse.getCode() != org.apache.hc.core5.http.HttpStatus.SC_OK) {
          proxyErrorMessageConsumer.accept(proxyResponse);
          return ResponseEntity.status(HttpStatus.valueOf(proxyResponse.getCode())).build();
        }

        final byte[] buffer = new byte[10240];
        final HttpEntity entity = proxyResponse.getEntity();
        final InputStream inputStream = entity.getContent();

        final boolean doStore = storageHelper.getDoStore(context.getRegistry());
        if (doStore && prepareCreationRunnable != null) {
          // FIXME: for performance reasons we stream to the client the same time we store
          // locally
          // so we create elements in database before they actually exist on drive
          prepareCreationRunnable.run();
        }

        final CloseableHolder responseCloseableHolder = new CloseableHolder(proxyResponse);
        closeableCleaner.add(responseCloseableHolder);

        doClose = false;
        final StreamingResponseBody stream = outputStream -> {
          try {
            T fileCreation = null;
            if (doStore) {
              fileCreation = initiateCreationSupplier.get();
            }
            try {
              int read;
              final ByteArrayOutputStream baos = new ByteArrayOutputStream();
              while ((read = inputStream.read(buffer)) != -1) {
                responseCloseableHolder.setLastUsed(System.currentTimeMillis());
                if (fileCreation != null) {
                  fileCreation.getOutputStream().write(buffer, 0, read);
                }
                outputStream.write(buffer, 0, read);
                baos.write(buffer, 0, read);
              }

              if (doStore) {
                createFileConsumer.accept(fileCreation);
              }
            } catch (final Throwable throwable) {
              if (doStore) {
                abortFileCreationConsumer.accept(fileCreation);
              }
              throw throwable;
            }
          } catch (final Throwable throwable) {
            LOGGER.error(throwable.getMessage(), throwable);
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

    return getLocalFileSupplier.get();
  }

  public boolean useCache(final MavenOperationContext context,
                          final Supplier<String> cacheKeySupplier) {
    final ProxyRegistry registry = (ProxyRegistry) context.getRegistry();
    final String cacheKey = cacheKeySupplier.get();
    return proxyCache.upToDate(registry, cacheKey);
  }
}
