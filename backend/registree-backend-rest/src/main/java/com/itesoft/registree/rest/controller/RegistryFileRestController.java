package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.FileController;
import com.itesoft.registree.dto.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/registries/{registryName}/files")
public class RegistryFileRestController {
  @Autowired
  private FileController fileController;

  @RequestMapping(value = "/{path}",
                  method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<File> getFile(@PathVariable("registryName") final String registryName,
                                      @PathVariable("path") final String path) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final File file = fileController.getFile(createDefaultRequestContext(),
                                             responseContext,
                                             registryName,
                                             path);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(file);
  }
}
