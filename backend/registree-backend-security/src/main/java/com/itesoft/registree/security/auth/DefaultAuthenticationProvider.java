package com.itesoft.registree.security.auth;

import java.util.Base64;
import java.util.Optional;
import java.util.Set;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;
import com.itesoft.registree.security.Scheme;
import com.itesoft.registree.security.token.TokenService;

import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultAuthenticationProvider implements AuthenticationProvider {
  @Autowired
  private TokenService tokenService;

  @Autowired
  @Lazy
  private UserAuthenticationProviders userAuthenticationProviders;

  @Autowired
  private UserRepository userRepository;

  @Override
  public Set<Scheme> getSupportedSchemes() {
    return Set.of(Scheme.BASIC,
                  Scheme.BEARER);
  }

  @Override
  public AuthenticationResult authenticate(final Scheme scheme, final String parameters) {
    RegistreeUserDetails userDetails = null;
    switch (scheme) {
    case BASIC:
      userDetails = basicAuth(parameters);
      break;
    case BEARER:
      userDetails = bearerAuth(parameters);
      break;
    default:
    }

    if (userDetails == null) {
      return AuthenticationResult.builder()
        .authenticated(false)
        .build();
    }

    return AuthenticationResult.builder()
      .authenticated(true)
      .userDetails(userDetails)
      .build();
  }

  private RegistreeUserDetails basicAuth(final String parameters) {
    final byte[] usernameAndPasswordAsBytes;
    try {
      usernameAndPasswordAsBytes = Base64.getDecoder().decode(parameters);
    } catch (final IllegalArgumentException exception) {
      return null;
    }
    final String usernameAndPassword = new String(usernameAndPasswordAsBytes);
    final String[] tab = usernameAndPassword.split(":", 2);
    if (tab.length != 2) {
      return null;
    }
    final String username = tab[0];
    final String password = tab[1];

    final Long userId = userAuthenticationProviders.authenticate(username, password);
    if (userId == null) {
      return null;
    }

    return RegistreeUserDetails.builder().id(userId).username(username).build();
  }

  private RegistreeUserDetails bearerAuth(final String parameters) {
    final Long userId = tokenService.getTokenUserId(parameters);
    if (userId == null) {
      // FIXME: ugly hack for npm to work since npm does not have a way to npm login
      // in script
      // we put basic auth in .npmrc and npm cli passes it as a Bearer token
      return basicAuth(parameters);
    }
    final Optional<UserEntity> userEntity = userRepository.findById(userId);
    if (userEntity.isEmpty()) {
      return null;
    }
    final UserEntity user = userEntity.get();
    return RegistreeUserDetails.builder()
      .id(user.getId())
      .username(user.getUsername())
      .build();
  }
}
