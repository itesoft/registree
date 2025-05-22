package com.itesoft.registree.security.auth.user;

import java.util.Optional;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(-1000)
public class BuiltinUserAuthenticationProvider implements UserAuthenticationProvider {
  @Autowired
  private UserRepository userRepository;

  @Override
  public Long authenticate(final String username, final String password) {
    final Optional<UserEntity> userEntity = userRepository.findByUsernameAndPassword(username, password);
    if (userEntity.isEmpty()) {
      return null;
    }
    return userEntity.get().getId();
  }
}
