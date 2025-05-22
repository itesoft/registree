package com.itesoft.registree.security.token;

import java.util.Base64;
import java.util.UUID;

import com.itesoft.registree.java.ExpirableCache;
import com.itesoft.registree.security.auth.RegistreeAuthentication;
import com.itesoft.registree.security.auth.RegistreeUserDetails;
import com.itesoft.registree.security.auth.UserAuthenticationProviders;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
  @Autowired
  private UserAuthenticationProviders userAuthenticationProviders;

  // TODO: persist tokens
  private final ExpirableCache<String, Long> tokens = new ExpirableCache<>(600000);

  public String getToken(final String token) {
    if (!tokens.containsKey(token)) {
      return null;
    }
    tokens.refresh(token);
    return token;
  }

  public Long getTokenUserId(final String token) {
    tokens.refresh(token);
    return tokens.get(token);
  }

  public String createToken() throws IllegalAccessException {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final RegistreeAuthentication authentication = (RegistreeAuthentication) securityContext.getAuthentication();
    final RegistreeUserDetails userDetails = (RegistreeUserDetails) authentication.getDetails();

    return doCreateToken(userDetails.getId());
  }

  public String createToken(final String username,
                            final String password) {
    final Long userId = userAuthenticationProviders.authenticate(username, password);
    if (userId == null) {
      return null;
    }
    return doCreateToken(userId);
  }

  public void deleteToken(final String token) {
    tokens.remove(token);
  }

  // TODO: remove this method when tokens are persisted
  public String createBasicToken(final String username,
                                 final String password) {
    final Long userId = userAuthenticationProviders.authenticate(username, password);
    if (userId == null) {
      return null;
    }

    return Base64.getEncoder()
      .encodeToString(String.format("%s:%s",
                                    username,
                                    password)
        .getBytes());
  }

  private String doCreateToken(final long userId) {
    final String token = UUID.randomUUID().toString();
    tokens.put(token, userId);
    return token;
  }
}
