package com.itesoft.registree.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itesoft.registree.dto.ProxyRegistry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.springframework.stereotype.Component;

@Component
public class ProxyCache {

  private final Map<String, Map<String, Long>> expirationMap = new ConcurrentHashMap<>();

  public void clear(final String registryName) {
    synchronized (expirationMap) {
      expirationMap.remove(registryName);
    }
  }

  @WithSpan
  public boolean upToDate(final ProxyRegistry proxyRegistry,
                          final String key) {
    final boolean result = doUpToDate(proxyRegistry, key);
    Span.current().addEvent("up-to-date", Attributes.of(AttributeKey.booleanKey("is up to date"), result));
    return result;
  }

  private boolean doUpToDate(final ProxyRegistry proxyRegistry,
                             final String key) {
    if (!proxyRegistry.isDoStore()) {
      return false;
    }

    final int cacheTimeout = proxyRegistry.getCacheTimeout();
    if (cacheTimeout == 0) {
      return false;
    }

    final String registryName = proxyRegistry.getName();
    final long delay = Long.valueOf(cacheTimeout) * 60 * 1000;
    final long now = System.currentTimeMillis();
    Map<String, Long> expirationPerKey = expirationMap.get(registryName);
    if (expirationPerKey == null) {
      synchronized (expirationMap) {
        expirationPerKey = expirationMap.get(registryName);
        if (expirationPerKey == null) {
          expirationPerKey = new ConcurrentHashMap<>();
          expirationMap.put(registryName, expirationPerKey);
        }
      }
    }

    final Long expirationTime = expirationPerKey.get(key);
    if (expirationTime == null || expirationTime < now) {
      expirationPerKey.put(key, now + delay);
      return false;
    }
    return true;
  }
}
