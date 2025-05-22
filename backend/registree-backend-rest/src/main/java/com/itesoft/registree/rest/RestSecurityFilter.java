package com.itesoft.registree.rest;

import static com.itesoft.registree.persistence.WellKnownRoutePaths.API_V1_ROUTE_PATH;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.itesoft.registree.acl.AclService;
import com.itesoft.registree.acl.Permission;
import com.itesoft.registree.web.WebPathsByPortConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RestSecurityFilter extends OncePerRequestFilter {
  @Autowired
  private AclService aclService;

  @Autowired
  private WebPathsByPortConfiguration webPathsByPortConfiguration;

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
                                  final HttpServletResponse response,
                                  final FilterChain filterChain)
    throws ServletException, IOException {
    final String path = request.getServletPath();
    final int port = request.getLocalPort();
    if (webPathsByPortConfiguration.getDefaultPort() == port
      && path.startsWith(API_V1_ROUTE_PATH)) {
      final String method = request.getMethod();
      final Permission permission;
      if (HttpMethod.HEAD.matches(method)
        || HttpMethod.GET.matches(method)) {
        permission = Permission.READ;
      } else if (HttpMethod.POST.matches(method)
        || HttpMethod.PUT.matches(method)) {
        permission = Permission.WRITE;
      } else if (HttpMethod.DELETE.matches(method)) {
        permission = Permission.DELETE;
      } else {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return;
      }

      final boolean isAccessAuthorized = aclService.isAccessAuthorized(path,
                                                                       permission);
      if (!isAccessAuthorized) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return;
      }
    }

    filterChain.doFilter(request, response);
  }
}
