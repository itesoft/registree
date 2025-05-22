package com.itesoft.registree.console.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.CommandController;
import com.itesoft.registree.console.dto.ExecuteCommandResult;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.User;
import com.itesoft.registree.exception.NotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CommandUserDeleteTest {
  @Autowired
  private CommandController commandController;

  @Autowired
  private UserController userController;

  @Test
  public void deleteUser() throws Exception {
    final String name = "deleteUser";
    ExecuteCommandResult executeCommandResult = commandController.execute(RequestContext.builder().build(),
                                                                          ResponseContext.builder().build(),
                                                                          "user-create",
                                                                          name,
                                                                          "password");
    assertEquals(0, executeCommandResult.getExitCode());
    final User user =
      userController.getUser(RequestContext.builder().build(),
                             ResponseContext.builder().build(),
                             OneOfLongOrString.from(name));
    assertEquals(name, user.getUsername());
    assertNull(user.getFirstName());
    assertNull(user.getLastName());

    executeCommandResult = commandController.execute(RequestContext.builder().build(),
                                                     ResponseContext.builder().build(),
                                                     "user-delete",
                                                     name);
    assertEquals(0, executeCommandResult.getExitCode());

    assertThrows(NotFoundException.class,
                 () -> userController.getUser(RequestContext.builder().build(),
                                              ResponseContext.builder().build(),
                                              OneOfLongOrString.from(name)));
  }
}
