package com.itesoft.registree.security;

import com.itesoft.registree.persistence.WellKnownUsers;
import com.itesoft.registree.security.auth.RegistreeAuthentication;
import com.itesoft.registree.security.auth.RegistreeUserDetails;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
  public String getUsername() {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return WellKnownUsers.ANONYMOUS_USERNAME;
    }

    final RegistreeAuthentication registreeAuthentication = (RegistreeAuthentication) authentication;
    final RegistreeUserDetails registreeUserDetails = (RegistreeUserDetails) registreeAuthentication.getDetails();
    return registreeUserDetails.getUsername();
  }
}
