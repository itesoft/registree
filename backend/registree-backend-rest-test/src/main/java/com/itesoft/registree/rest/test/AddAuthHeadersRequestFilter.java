package com.itesoft.registree.rest.test;

import java.io.IOException;
import java.util.Base64;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;

public class AddAuthHeadersRequestFilter implements ClientRequestFilter {
  private final String username;
  private final String password;

  public AddAuthHeadersRequestFilter(final String username, final String password) {
    this.username = username;
    this.password = password;
  }

  @Override
  public void filter(final ClientRequestContext requestContext) throws IOException {
    final String token = username + ":" + password;
    final String base64Token = Base64.getEncoder().encodeToString(token.getBytes());
    requestContext.getHeaders().add("Authorization", "Basic " + base64Token);
  }
}
