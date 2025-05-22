package com.itesoft.registree.security.auth;

import static com.itesoft.registree.persistence.WellKnownUsers.ADMIN_USERNAME;
import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import com.itesoft.registree.security.Scheme;
import com.itesoft.registree.security.token.TokenService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootTest
public class AuthenticationServiceTest {
  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private TokenService tokenService;

  @Test
  public void anonymousAuthentication() {
    authenticationService.authenticate(null, null);
    assertAuthenticated(ANONYMOUS_USERNAME);
  }

  @Test
  public void builtinUserAuthentication() {
    final String auth = Base64.getEncoder().encodeToString("admin:admin".getBytes());
    authenticationService.authenticate(Scheme.BASIC, auth);
    assertAuthenticated(ADMIN_USERNAME);
  }

  @Test
  public void ldapUserAuthentication() {
    final String auth = Base64.getEncoder().encodeToString("bsummers:test".getBytes());
    authenticationService.authenticate(Scheme.BASIC, auth);
    assertAuthenticated("bsummers");
  }

  @Test
  public void tokenAuthentication() {
    final String token = tokenService.createToken(ADMIN_USERNAME, "admin");
    authenticationService.authenticate(Scheme.BEARER, token);
    assertAuthenticated(ADMIN_USERNAME);
  }

  private void assertAuthenticated(final String username) {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();
    assertNotNull(authentication);
    assertTrue(authentication.isAuthenticated());
    final Object details = authentication.getDetails();
    assertNotNull(details);
    assertInstanceOf(RegistreeUserDetails.class, details);
    final RegistreeUserDetails userDetails = (RegistreeUserDetails) details;
    assertEquals(username, userDetails.getUsername());
  }
}
