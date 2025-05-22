package com.itesoft.registree.acl;

import java.util.List;

import com.itesoft.registree.dao.jpa.RouteEntity;
import com.itesoft.registree.dao.jpa.RouteRepository;
import com.itesoft.registree.security.auth.RegistreeAuthentication;
import com.itesoft.registree.security.auth.RegistreeUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AclService {
  @Autowired
  private RouteRepository repository;

  public boolean isAccessAuthorized(final String path,
                                    final Permission permission) {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }

    final RegistreeAuthentication registreeAuthentication = (RegistreeAuthentication) authentication;
    final RegistreeUserDetails registreeUserDetails = (RegistreeUserDetails) registreeAuthentication.getDetails();
    final long userId = registreeUserDetails.getId();

    final List<RouteEntity> routes = repository.findAllByUserIdAndPermissionsContaining(userId, permission.toChar());
    for (final RouteEntity route : routes) {
      if (path.startsWith(route.getPath())) {
        return true;
      }
    }

    return false;
  }
}
