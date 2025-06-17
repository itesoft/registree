package com.itesoft.registree.oci.rest.proxy.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itesoft.registree.oci.config.OciProxyRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OciProxyAuthenticationManager {
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private HttpClient httpClient;

  private class AuthHeader {
    private final String authorizationHeader;
    private final long expirationTime;

    AuthHeader(final String authorizationHeader,
               final long expirationTime) {
      this.authorizationHeader = authorizationHeader;
      this.expirationTime = expirationTime;
    }

    String getAuthorizationHeader() {
      return authorizationHeader;
    }

    boolean isExpired() {
      return System.currentTimeMillis() > expirationTime;
    }
  }

  private static final String PULL_SCOPE_PARAMETER_VALUE = "repository:%s:pull";
  private static final int DEFAULT_EXPIRATION = 60;

  private final Map<String, String> basicAuthenticationCache = new ConcurrentHashMap<>();
  private final Map<String, Map<String, AuthHeader>> bearerAuthenticationCache = new ConcurrentHashMap<>();

  public boolean addAuthentication(final ClassicHttpRequest httpRequest,
                                   final OciProxyRegistry proxyRegistry,
                                   final String name)
    throws Exception {
    Map<String, AuthHeader> authPerName = bearerAuthenticationCache.get(proxyRegistry.getName());
    if (authPerName != null) {
      final AuthHeader authHeader = authPerName.get(name);
      if (authHeader != null && !authHeader.isExpired()) {
        httpRequest.addHeader(HttpHeaders.AUTHORIZATION,
                              authHeader.getAuthorizationHeader());
        return true;
      }
    } else {
      authPerName = new ConcurrentHashMap<>();
    }
    bearerAuthenticationCache.put(proxyRegistry.getName(), authPerName);

    boolean addBasicAuth = false;
    final HttpGet httpGet = new HttpGet(String.format("%s/v2/", proxyRegistry.getProxyUrl()));

    final String wwwAuthenticate;
    try (ClassicHttpResponse response = httpClient.executeOpen(null, httpGet, null)) {
      if (response.getCode() == HttpStatus.SC_OK) {
        return true;
      }

      if (response.getCode() != HttpStatus.SC_UNAUTHORIZED) {
        return false;
      }

      final Header wwwAuthenticateHeader = response.getHeader(HttpHeaders.WWW_AUTHENTICATE);
      wwwAuthenticate = wwwAuthenticateHeader.getValue();
    }

    if (wwwAuthenticate.startsWith("Basic ")) {
      addBasicAuth = true;
    } else {
      return bearerAuth(httpRequest,
                        proxyRegistry,
                        name,
                        authPerName,
                        httpClient,
                        wwwAuthenticate);
    }

    if (addBasicAuth) {
      final String username = proxyRegistry.getProxyUsername();
      if (username == null) {
        return true;
      }

      addBasicAuthorization(httpRequest, proxyRegistry);

      return true;
    }

    return false;
  }

  private boolean bearerAuth(final ClassicHttpRequest httpRequest,
                             final OciProxyRegistry proxyRegistry,
                             final String name,
                             final Map<String, AuthHeader> authPerName,
                             final HttpClient httpClient,
                             final String wwwAuthenticate)
    throws URISyntaxException, IOException {
    final String wwwAuthenticateParametersAsString = wwwAuthenticate.substring("Bearer ".length());

    final Map<String, String> parameters = new HashMap<>();
    Arrays.stream(wwwAuthenticateParametersAsString.split(","))
      .forEach(param -> {
        final String[] tab = param.split("=", 2);
        final String key = tab[0];
        String value = tab[1];
        if ((value.startsWith("\"") || value.startsWith("'"))
          && (value.endsWith("\"") || value.endsWith("'"))) {
          value = value.substring(1, value.length() - 1);
        }
        parameters.put(key, value);
      });

    final String authUrl = parameters.get("realm");
    final URIBuilder uriBuilder = new URIBuilder(authUrl);
    for (final Map.Entry<String, String> entry : parameters.entrySet()) {
      if ("realm".equals(entry.getKey())) {
        continue;
      }
      uriBuilder.addParameter(entry.getKey(), entry.getValue());
    }
    uriBuilder.addParameter("scope",
                            String.format(PULL_SCOPE_PARAMETER_VALUE,
                                          name));
    final URI uri = uriBuilder.build();

    final HttpGet httpGet = new HttpGet(uri);
    if (proxyRegistry.getProxyUsername() != null) {
      addBasicAuthorization(httpGet, proxyRegistry);
    }

    final ProxyAuth proxyAuth = httpClient.execute(httpGet, response -> {
      if (response.getCode() != HttpStatus.SC_OK) {
        return null;
      }
      final byte[] bytes = EntityUtils.toByteArray(response.getEntity());
      return objectMapper.readValue(bytes, ProxyAuth.class);
    });

    if (proxyAuth == null) {
      return false;
    }

    final String authorization = proxyAuth.getToken();
    final String authorizationHeader = "Bearer " + authorization;
    httpRequest.addHeader(HttpHeaders.AUTHORIZATION,
                          authorizationHeader);
    Integer expiresIn = proxyAuth.getExpiresIn();
    if (expiresIn == null) {
      expiresIn = DEFAULT_EXPIRATION;
    }

    authPerName.put(name,
                    new AuthHeader(authorizationHeader,
                                   System.currentTimeMillis() + (expiresIn * 1000)));

    return true;
  }

  private void addBasicAuthorization(final ClassicHttpRequest httpRequest,
                                     final OciProxyRegistry proxyRegistry) {
    final String valueToEncode = proxyRegistry.getProxyUsername() + ":" + proxyRegistry.getProxyPassword();
    final String authorization = "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    httpRequest.addHeader(HttpHeaders.AUTHORIZATION, authorization);
    basicAuthenticationCache.put(proxyRegistry.getName(), authorization);
  }
}
