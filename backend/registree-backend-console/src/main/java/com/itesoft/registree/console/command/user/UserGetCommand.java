package com.itesoft.registree.console.command.user;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.console.command.AbstractGetCommand;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.OneOfLongOrString;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "user-get",
         description = "Gets a user")
public class UserGetCommand extends AbstractGetCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The id or name of the user")
  private String idOrName;

  @Autowired
  private UserController userController;

  @Override
  public Integer call() throws Exception {
    doCall("User",
           () -> userController.getUser(RequestContext.builder().build(),
                                        ResponseContext.builder().build(),
                                        OneOfLongOrString.valueOf(idOrName)));
    return 0;
  }
}
