package com.itesoft.registree.security.auth;

import java.util.List;

import com.itesoft.registree.security.auth.user.UserAuthenticationProvider;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationProviders {
  @Autowired
  @Lazy
  private List<UserAuthenticationProvider> userAuthenticationProviders;

  public Long authenticate(final String username, final String password) {
    for (final UserAuthenticationProvider authenticationProvider : userAuthenticationProviders) {
      final Long userId = authenticationProvider.authenticate(username, password);
      if (userId != null) {
        return userId;
      }
    }
    return null;
  }
}
