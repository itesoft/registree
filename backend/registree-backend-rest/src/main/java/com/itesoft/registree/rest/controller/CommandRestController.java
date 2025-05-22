package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.console.CommandController;
import com.itesoft.registree.console.dto.ExecuteCommandArgs;
import com.itesoft.registree.console.dto.ExecuteCommandResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/commands")
public class CommandRestController {
  @Autowired
  private CommandController commandController;

  @RequestMapping(method = RequestMethod.POST,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<ExecuteCommandResult> executeCommand(@RequestBody final ExecuteCommandArgs executeCommandArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final ExecuteCommandResult executeCommandResult = commandController.execute(createDefaultRequestContext(),
                                                                                responseContext,
                                                                                executeCommandArgs);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(executeCommandResult);
  }
}
