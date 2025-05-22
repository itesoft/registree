package com.itesoft.registree.security.auth.user;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LdapUserAuthenticationProviderTest {
  @Autowired
  private LdapUserAuthenticationProvider ldapUserAuthenticationProvider;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void authenticationSuccess() {
    final Long userId = ldapUserAuthenticationProvider.authenticate("bsummers", "test");
    assertNotNull(userId);
    final UserEntity userEntity = userRepository.findByUsername("bsummers").get();
    assertEquals(userId, userEntity.getId());
    assertEquals("bsummers", userEntity.getUsername());
    assertEquals("Buffy", userEntity.getFirstName());
    assertEquals("Summers", userEntity.getLastName());

    assertEquals(userId,
                 ldapUserAuthenticationProvider.authenticate("bsummers", "test"));
  }

  @Test
  public void authenticationSuccessWithIncompleteUser() {
    final Long userId = ldapUserAuthenticationProvider.authenticate("wrosenbe", "abc");
    assertNotNull(userId);
    final UserEntity userEntity = userRepository.findByUsername("wrosenbe").get();
    assertEquals(userId, userEntity.getId());
    assertEquals("wrosenbe", userEntity.getUsername());
    assertNull(userEntity.getFirstName());
    assertEquals("Rosenberg", userEntity.getLastName());
  }

  @Test
  public void authenticationFailureWithKnownUser() {
    final Long userId = ldapUserAuthenticationProvider.authenticate("bsummers", "fail");
    assertNull(userId);
  }

  @Test
  public void authenticationFailureWithUnknownUser() {
    final Long userId = ldapUserAuthenticationProvider.authenticate("unk", "fail");
    assertNull(userId);
  }
}
