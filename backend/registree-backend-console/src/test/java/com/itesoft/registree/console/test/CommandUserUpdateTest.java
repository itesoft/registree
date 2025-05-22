package com.itesoft.registree.console.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.CommandController;
import com.itesoft.registree.console.dto.ExecuteCommandResult;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CommandUserUpdateTest {
  @Autowired
  private CommandController commandController;

  @Autowired
  private UserController userController;

  @Test
  public void upateUserWithFirstNameAndLastName() throws Exception {
    final String name = "upateUserWithFirstNameAndLastName";
    final String firstName = "first";
    final String lastName = "last";

    ExecuteCommandResult executeCommandResult = commandController.execute(RequestContext.builder().build(),
                                                                          ResponseContext.builder().build(),
                                                                          "user-create",
                                                                          name,
                                                                          "password");
    assertEquals(0, executeCommandResult.getExitCode());
    User user =
      userController.getUser(RequestContext.builder().build(),
                             ResponseContext.builder().build(),
                             OneOfLongOrString.from(name));
    assertEquals(name, user.getUsername());
    assertNull(user.getFirstName());
    assertNull(user.getLastName());

    executeCommandResult = commandController.execute(RequestContext.builder().build(),
                                                     ResponseContext.builder().build(),
                                                     "user-update",
                                                     "-f",
                                                     firstName,
                                                     "-l",
                                                     lastName,
                                                     name);
    assertEquals(0, executeCommandResult.getExitCode());

    user =
      userController.getUser(RequestContext.builder().build(),
                             ResponseContext.builder().build(),
                             OneOfLongOrString.from(name));
    assertEquals(name, user.getUsername());
    assertEquals(firstName, user.getFirstName());
    assertEquals(lastName, user.getLastName());
  }
}
