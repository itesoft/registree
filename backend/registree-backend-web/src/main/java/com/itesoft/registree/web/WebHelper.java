package com.itesoft.registree.web;

import jakarta.servlet.http.HttpServletRequest;

public abstract class WebHelper {
  public static String getHost(final HttpServletRequest request) {
    final StringBuffer url = request.getRequestURL();
    final String uri = request.getRequestURI();
    final String host = url.substring(0, url.indexOf(uri));

    final String forwardedProto = request.getHeader("x-forwarded-proto");
    if (forwardedProto != null) {
      final String scheme = request.getScheme();
      return host.replace(scheme + "://", forwardedProto + "://");
    }
    return host;
  }

  private WebHelper() {
  }
}
