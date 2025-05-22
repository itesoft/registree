package com.itesoft.registree.console.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.CommandController;
import com.itesoft.registree.console.dto.ExecuteCommandResult;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CommandUserGetAndSearchTest {
  @Autowired
  private CommandController commandController;

  @Test
  public void getUser() throws Exception {
    final String name = "getUser";
    ExecuteCommandResult executeCommandResult = commandController.execute(RequestContext.builder().build(),
                                                                          ResponseContext.builder().build(),
                                                                          "user-create",
                                                                          name,
                                                                          "password");
    assertEquals(0, executeCommandResult.getExitCode());
    executeCommandResult =
      commandController.execute(RequestContext.builder().build(),
                                ResponseContext.builder().build(),
                                "user-get",
                                name);
    assertEquals(0, executeCommandResult.getExitCode());
    System.out.println(executeCommandResult.getOutput());
  }

  @Test
  public void searchUsers() throws Exception {
    final ExecuteCommandResult executeCommandResult =
      commandController.execute(RequestContext.builder().build(),
                                ResponseContext.builder().build(),
                                "user-search");
    assertEquals(0, executeCommandResult.getExitCode());
    System.out.println(executeCommandResult.getOutput());
  }
}
