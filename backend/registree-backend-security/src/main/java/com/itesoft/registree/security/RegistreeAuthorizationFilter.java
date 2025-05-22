package com.itesoft.registree.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.itesoft.registree.security.auth.AuthenticationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(-2000)
public class RegistreeAuthorizationFilter extends OncePerRequestFilter {
  private static final String BASIC_AUTHORIZATION = "Basic ";
  private static final String BEARER_AUTHORIZATION = "Bearer ";

  @Autowired
  private AuthenticationService authenticationService;

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final FilterChain chain)
    throws ServletException, IOException {
    final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    final Scheme scheme;
    final String parameters;
    if (authorization == null || authorization.isEmpty()) {
      scheme = Scheme.NONE;
      parameters = null;
    } else {
      if (authorization.toLowerCase().startsWith(BASIC_AUTHORIZATION.toLowerCase())) {
        scheme = Scheme.BASIC;
        parameters = authorization.substring(BASIC_AUTHORIZATION.length());
      } else if (authorization.toLowerCase().startsWith(BEARER_AUTHORIZATION.toLowerCase())) {
        scheme = Scheme.BEARER;
        parameters = authorization.substring(BEARER_AUTHORIZATION.length());
      } else {
        scheme = Scheme.RAW;
        parameters = authorization;
      }
    }

    try {
      authenticationService.authenticate(scheme, parameters);
      chain.doFilter(request, response);
    } finally {
      authenticationService.clearAuthentication();
    }
  }
}
