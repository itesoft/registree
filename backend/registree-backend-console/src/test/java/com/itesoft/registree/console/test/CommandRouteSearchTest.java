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
public class CommandRouteSearchTest {
  @Autowired
  private CommandController commandController;

  @Test
  public void searchRoutes() {
    final ExecuteCommandResult executeCommandResult = commandController.execute(RequestContext.builder().build(),
                                                                                ResponseContext.builder().build(),
                                                                                "route-search");
    assertEquals(0, executeCommandResult.getExitCode());
    System.out.println(executeCommandResult.getOutput());
  }
}
