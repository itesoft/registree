package com.itesoft.registree.registry.filtering;

import java.util.List;

import com.itesoft.registree.dto.ProxyRegistry;
import com.itesoft.registree.dto.ProxyRegistryFilter;
import com.itesoft.registree.dto.ProxyRegistryFilterPolicy;
import com.itesoft.registree.dto.ProxyRegistryFiltering;

import org.springframework.stereotype.Component;

@Component
public class ProxyFilteringService {
  public boolean included(final ProxyRegistry proxyRegistry,
                          final String path) {
    final ProxyRegistryFiltering filtering = proxyRegistry.getFiltering();
    if (filtering == null) {
      return true;
    }

    final List<ProxyRegistryFilter> filters = filtering.getFilters();
    for (final ProxyRegistryFilter filter : filters) {
      final String filterPath = filter.getPathPrefix();
      if (path.startsWith(filterPath)) {
        return ProxyRegistryFilterPolicy.INCLUDE.equals(filter.getPolicy());
      }
    }

    return ProxyRegistryFilterPolicy.INCLUDE.equals(filtering.getDefaultPolicy());
  }
}
