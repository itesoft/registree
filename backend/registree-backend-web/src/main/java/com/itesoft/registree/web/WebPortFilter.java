package com.itesoft.registree.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class WebPortFilter extends OncePerRequestFilter {
  @Autowired
  private WebPathsByPortConfiguration webPathsByPortConfiguration;

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final FilterChain filterChain)
    throws ServletException, IOException {
    if (!webPathsByPortConfiguration.matches(request)) {
      response.setStatus(HttpStatus.NOT_FOUND.value());
      return;
    }

    filterChain.doFilter(request, response);
  }
}
