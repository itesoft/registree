package com.itesoft.registree.console.command.user;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.console.command.AbstractSearchCommand;
import com.itesoft.registree.controller.UserController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "user-search",
         description = "Searches users")
public class UserSearchCommand extends AbstractSearchCommand implements RegistreeCommand {
  @Autowired
  private UserController userController;

  @Override
  public Integer call() throws Exception {
    doCall("users",
           () -> userController.searchUsers(RequestContext.builder().build(),
                                            ResponseContext.builder().build(),
                                            filter,
                                            sort,
                                            page,
                                            pageSize));
    return 0;
  }
}
