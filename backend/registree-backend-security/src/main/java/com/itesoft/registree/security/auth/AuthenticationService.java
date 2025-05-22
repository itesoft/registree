package com.itesoft.registree.security.auth;

import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;

import java.util.List;
import java.util.Set;

import com.itesoft.registree.dao.jpa.UserRepository;
import com.itesoft.registree.security.Scheme;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
  @Autowired
  @Lazy
  private List<AuthenticationProvider> authenticationProviders;

  @Autowired
  private UserRepository userRepository;

  public boolean isAuthenticated() {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final RegistreeAuthentication authentication = (RegistreeAuthentication) securityContext.getAuthentication();
    return authentication.isAuthenticated();
  }

  public void authenticate(final Scheme scheme,
                           final String parameters) {
    final Authentication authentication = getAuthentication(scheme, parameters);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  public void clearAuthentication() {
    SecurityContextHolder.clearContext();
  }

  private Authentication getAuthentication(final Scheme scheme,
                                           final String parameters) {
    if (parameters == null) {
      return anonymousAuthentication();
    }

    AuthenticationResult authenticationResult = null;
    for (final AuthenticationProvider authenticationProvider : authenticationProviders) {
      final Set<Scheme> supportedSchemes = authenticationProvider.getSupportedSchemes();
      if (supportedSchemes.contains(scheme)) {
        authenticationResult = authenticationProvider.authenticate(scheme, parameters);
        if (authenticationResult.isAuthenticated()) {
          break;
        }
      }
    }

    if (authenticationResult == null) {
      return anonymousAuthentication();
    }

    // TODO: do not switch to anonymous, return unauthorized
    if (!authenticationResult.isAuthenticated()) {
      return anonymousAuthentication();
    }

    return RegistreeAuthentication.builder()
      .isAuthenticated(true)
      .details(authenticationResult.getUserDetails())
      .build();
  }

  private Authentication anonymousAuthentication() {
    // TODO: no need to do this each time, keep id in cache
    final long userId = userRepository.findByUsername(ANONYMOUS_USERNAME).get().getId();

    final RegistreeUserDetails anonymousUser = RegistreeUserDetails.builder()
      .id(userId)
      .username(ANONYMOUS_USERNAME)
      .build();
    return RegistreeAuthentication.builder()
      .isAuthenticated(true)
      .details(anonymousUser)
      .build();
  }
}
