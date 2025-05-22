package com.itesoft.registree.rest.controller;

import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultRequestContext;
import static com.itesoft.registree.rest.controller.ContextHelper.createDefaultResponseContext;
import static com.itesoft.registree.rest.helper.SpringWebHelper.getHeaders;

import java.util.List;

import com.itesoft.registree.api.definition.ResponseContext;
import com.itesoft.registree.controller.FileController;
import com.itesoft.registree.dto.CreateFileArgs;
import com.itesoft.registree.dto.DeleteFileArgs;
import com.itesoft.registree.dto.File;
import com.itesoft.registree.dto.UpdateFileArgs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
public class FileRestController {
  @Autowired
  private FileController fileController;

  @RequestMapping(method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<List<File>> searchFiles(@RequestParam(value = "filter", required = false) final String filter,
                                                @RequestParam(value = "sort", required = false) final String sort,
                                                @RequestParam(value = "page", required = false, defaultValue = "0") final Integer page,
                                                @RequestParam(value = "page_size", required = false, defaultValue = "20") final Integer pageSize) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final List<File> files = fileController.searchFiles(createDefaultRequestContext(),
                                                        responseContext,
                                                        filter,
                                                        sort,
                                                        page,
                                                        pageSize);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(files);
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.GET,
                  produces = { "application/json" })
  public ResponseEntity<File> getFile(@PathVariable("id") final String id) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final File file = fileController.getFile(createDefaultRequestContext(),
                                             responseContext,
                                             id);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(file);
  }

  @RequestMapping(method = RequestMethod.POST,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<File> createFile(@RequestBody final CreateFileArgs createFileArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final File file = fileController.createFile(createDefaultRequestContext(),
                                                responseContext,
                                                createFileArgs);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(file);
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.PUT,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<File> updateFile(@PathVariable("id") final String id,
                                         @RequestBody final UpdateFileArgs updateFileArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    final File file = fileController.updateFile(createDefaultRequestContext(),
                                                responseContext,
                                                id,
                                                updateFileArgs);
    return ResponseEntity.ok()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .body(file);
  }

  @RequestMapping(value = "/{id}",
                  method = RequestMethod.DELETE,
                  consumes = { "application/json" },
                  produces = { "application/json" })
  public ResponseEntity<Void> deleteFile(@PathVariable("id") final String id,
                                         @RequestBody(required = false) final DeleteFileArgs deleteFileArgs) {
    final ResponseContext responseContext = createDefaultResponseContext();
    fileController.deleteFile(createDefaultRequestContext(),
                              responseContext,
                              id,
                              deleteFileArgs);
    return ResponseEntity.noContent()
      .headers(getHeaders(responseContext.getExtraProperties()))
      .build();
  }
}
