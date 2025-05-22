package com.itesoft.registree.npm.rest.proxy.auth;

import com.itesoft.registree.npm.config.NpmProxyRegistry;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class NpmProxyAuthenticationManager {
  public void addAuthentication(final ClassicHttpRequest httpRequest,
                                final NpmProxyRegistry proxyRegistry)
    throws Exception {
    final String authToken = proxyRegistry.getProxyAuthToken();
    if (authToken == null) {
      return;
    }

    final String authorizationHeader = "Bearer " + authToken;
    httpRequest.addHeader(HttpHeaders.AUTHORIZATION,
                          authorizationHeader);
  }
}
