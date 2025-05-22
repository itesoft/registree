package com.itesoft.registree.security.auth;

import java.util.Set;

import com.itesoft.registree.security.Scheme;

public interface AuthenticationProvider {
  Set<Scheme> getSupportedSchemes();

  AuthenticationResult authenticate(Scheme scheme,
                                    String parameters);
}
