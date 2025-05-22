package com.itesoft.registree;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.itesoft.registree.dao.jpa.UserEntity;
import com.itesoft.registree.dao.jpa.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class UsersCreationTest {
  @Autowired
  private UserRepository userRepository;

  @Test
  public void verifyUsersExist() {
    assertEquals(2, userRepository.count());
    boolean foundAnonymous = false;
    boolean foundAdmin = false;
    final Iterable<UserEntity> users = userRepository.findAll();
    for (final UserEntity userEntity : users) {
      final String username = userEntity.getUsername();
      assertNotNull(username);
      if ("anonymous".equals(username)) {
        foundAnonymous = true;
        assertNull(userEntity.getPassword());
      } else if ("admin".equals(username)) {
        foundAdmin = true;
        assertNotNull(userEntity.getPassword());
      } else {
        fail(String.format("Unexpected user %s", username));
      }
    }

    assertTrue(foundAnonymous);
    assertTrue(foundAdmin);
  }
}
