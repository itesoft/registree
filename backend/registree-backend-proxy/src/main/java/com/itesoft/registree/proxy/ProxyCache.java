package com.itesoft.registree.proxy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.registry.RegistriesStore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProxyCache {
  @Autowired
  private RegistriesStore registriesStore;

  private final Map<String, Map<String, Long>> expirationMap = new ConcurrentHashMap<>();

  public void clear(final String registryName) {
    synchronized (expirationMap) {
      expirationMap.remove(registryName);
    }
  }

  public boolean upToDate(final String registryName,
                          final String key) {
    final ProxyRegistry proxyRegistry = (ProxyRegistry) registriesStore.getRegistry(registryName);
    if (!proxyRegistry.isDoStore()) {
      return false;
    }

    final int cacheTimeout = proxyRegistry.getCacheTimeout();
    if (cacheTimeout == 0) {
      return false;
    }

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
