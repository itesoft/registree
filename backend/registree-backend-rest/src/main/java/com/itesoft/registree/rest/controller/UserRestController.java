package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.UserController;
import com.itesoft.registree.dto.CreateUserArgs;
import com.itesoft.registree.dto.DeleteUserArgs;
import com.itesoft.registree.dto.OneOfLongOrString;
import com.itesoft.registree.dto.UpdateUserArgs;
import com.itesoft.registree.dto.UpdateUserPasswordArgs;
import com.itesoft.registree.dto.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {
  @Autowired
  private UserController userController;

  @RequestMapping(method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<List<User>> searchUsers(@RequestParam(value = "filter", required = false) final String filter,
                                                @RequestParam(value = "sort", required = false) final String sort,
                                                @RequestParam(value = "page", required = false, defaultValue = "0") final Integer page,
                                                @RequestParam(value = "page_size", required = false, defaultValue = "20") final Integer pageSize) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final List<User> users = userController.searchUsers(createDefaultRequestContext(),
                                                        responseContext,
                                                        filter,
                                                        sort,
                                                        page,
                                                        pageSize);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(users);
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<User> getUser(@PathVariable("id") final OneOfLongOrString id) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final User user = userController.getUser(createDefaultRequestContext(),
                                             responseContext,
                                             id);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(user);
  }

  @RequestMapping(method = RequestMethod.POST,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<User> createUser(final HttpServletRequest request,
                                         @RequestBody final CreateUserArgs createUserArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final User user = userController.createUser(createDefaultRequestContext(),
                                                responseContext,
                                                createUserArgs);
    return ResponseEntity.created(null)
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(user);
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.PUT,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<User> updateUser(@PathVariable("id") final OneOfLongOrString id,
                                         @RequestBody final UpdateUserArgs updateUserArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final User user = userController.updateUser(createDefaultRequestContext(),
                                                responseContext,
                                                id,
                                                updateUserArgs);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(user);
  }

  @RequestMapping(value = "/{id}/password",
                  method = RequestMethod.PUT,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Void> updateUserPassword(@PathVariable("id") final OneOfLongOrString id,
                                                 @RequestBody final UpdateUserPasswordArgs updateUserPasswordArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    userController.updateUserPassword(createDefaultRequestContext(),
                                      responseContext,
                                      id,
                                      updateUserPasswordArgs);
    return ResponseEntity.noContent()
        .headers(getHeaders(responseContext.getExtraProperties()))
        .build();
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.DELETE,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Void> deleteUser(@PathVariable("id") final OneOfLongOrString id,
                                         @RequestBody(required = false) final DeleteUserArgs deleteUserArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    userController.deleteUser(createDefaultRequestContext(),
                              responseContext,
                              id,
                              deleteUserArgs);
    return ResponseEntity.noContent()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .build();
  }
}
