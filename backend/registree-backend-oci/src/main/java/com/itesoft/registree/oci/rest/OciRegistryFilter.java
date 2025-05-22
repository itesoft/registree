package com.itesoft.registree.oci.rest;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.itesoft.registree.oci.config.OciRegistries;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class OciRegistryFilter extends OncePerRequestFilter {
  @Lazy
  @Autowired
  private OciRegistries ociRegistries;

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final FilterChain filterChain)
    throws ServletException, IOException {
    filterChain.doFilter(request, response);

    final int port = request.getLocalPort();
    if (ociRegistries.isOciRegistry(port)) {
      response.addHeader("Docker-Distribution-Api-Version", "registry/2.0");
    }
  }
}
