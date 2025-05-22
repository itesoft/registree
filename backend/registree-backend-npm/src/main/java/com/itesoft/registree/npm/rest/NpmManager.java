package com.itesoft.registree.npm.rest;

import static com.itesoft.registree.web.WebHelper.getHost;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;

public interface NpmManager {
  RegistryType getType();

  default String getRegistryUri(final HttpServletRequest request) {
    final String host = getHost(request);
    return String.format("%s", host);
  }

  default String getFullPackageName(final String packageScope, final String packageName) {
    if (packageScope == null) {
      return packageName;
    } else {
      return String.format("%s/%s", packageScope, packageName);
    }
  }
}
