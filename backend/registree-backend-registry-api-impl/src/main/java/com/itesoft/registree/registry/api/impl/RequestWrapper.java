package com.itesoft.registree.registry.api.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RequestWrapper extends HttpServletRequestWrapper {
  private final String servletPathPrefix;

  public RequestWrapper(final HttpServletRequest request,
                        final String servletPathPrefix) {
    super(request);
    this.servletPathPrefix = servletPathPrefix;
  }

  @Override
  public String getRequestURI() {
    return super.getRequestURI().substring(servletPathPrefix.length());
  }

  @Override
  public String getServletPath() {
    return super.getServletPath().substring(servletPathPrefix.length());
  }
}
