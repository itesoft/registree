package com.itesoft.registree.persistence;

import static com.itesoft.registree.persistence.WellKnownUsers.ADMIN_USERNAME;
import static com.itesoft.registree.persistence.WellKnownUsers.ANONYMOUS_USERNAME;

import jakarta.annotation.PostConstruct;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DefaultUsersManager {
  @Autowired
  private UserRepository userRepository;

  @PostConstruct
  public void loadUsersToDatabase() {
    final boolean anonymousExists = userRepository.existsByUsername(ANONYMOUS_USERNAME);
    if (!anonymousExists) {
      final UserEntity anonymousUser = new UserEntity();
      anonymousUser.setUsername(ANONYMOUS_USERNAME);
      userRepository.save(anonymousUser);
    }

    final boolean adminExists = userRepository.existsByUsername(ADMIN_USERNAME);
    if (!adminExists) {
      final UserEntity adminUser = new UserEntity();
      adminUser.setUsername(ADMIN_USERNAME);
      adminUser.setPassword(ADMIN_USERNAME);
      userRepository.save(adminUser);
    }
  }
}
