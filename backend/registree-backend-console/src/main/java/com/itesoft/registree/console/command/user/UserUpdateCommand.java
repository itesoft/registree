package com.itesoft.registree.console.command.user;

import com.itesoft.registree.api.definition.RequestContext;
import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.RegistreeCommand;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.UpdateUserArgs;
import com.itesoft.registree.dto.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "user-update",
         description = "Updates a user")
public class UserUpdateCommand implements RegistreeCommand {
  @Parameters(index = "0",
              description = "The id or name of the user")
  private String idOrName;

  @Option(names = "-u",
          description = "The username")
  private String username;

  @Option(names = "-f",
          description = "The user first name")
  private String firstName;

  @Option(names = "-l",
          description = "The user last name")
  private String lastName;

  @Spec
  private CommandSpec spec;

  @Autowired
  private UserController userController;

  @Autowired
  private ConversionService conversionService;

  @Override
  public Integer call() throws Exception {
    final User user =
      userController.getUser(RequestContext.builder().build(),
                             ResponseContext.builder().build(),
                             OneOfLongOrString.valueOf(idOrName));

    final UpdateUserArgs updateUserArgs = conversionService.convert(user, UpdateUserArgs.class);
    if (username != null) {
      updateUserArgs.setUsername(username);
    }
    if (firstName != null) {
      updateUserArgs.setFirstName(firstName);
    }
    if (lastName != null) {
      updateUserArgs.setLastName(lastName);
    }
    userController.updateUser(RequestContext.builder().build(),
                              ResponseContext.builder().build(),
                              OneOfLongOrString.valueOf(idOrName),
                              updateUserArgs);
    spec.commandLine().getOut().println("User updated");
    return 0;
  }
}
