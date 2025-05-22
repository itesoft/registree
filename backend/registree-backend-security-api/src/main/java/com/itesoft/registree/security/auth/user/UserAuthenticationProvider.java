package com.itesoft.registree.security.auth.user;

public interface UserAuthenticationProvider {
  Long authenticate(String username, String password);
}
