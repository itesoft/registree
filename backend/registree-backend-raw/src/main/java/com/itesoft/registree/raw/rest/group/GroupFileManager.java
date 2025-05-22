package com.itesoft.registree.raw.rest.group;

import jakarta.servlet.http.HttpServletRequest;

import com.itesoft.registree.dto.RegistryType;
import com.itesoft.registree.raw.rest.RawFileManager;
import com.itesoft.registree.raw.rest.RawOperationContext;
import com.itesoft.registree.raw.rest.ReadOnlyRawFileManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Component
public class GroupFileManager extends ReadOnlyRawFileManager implements RawFileManager {
  @Autowired
  private RawGroupRegistryHelper groupRegistryHelper;

  @Override
  public RegistryType getType() {
    return RegistryType.GROUP;
  }

  @Override
  public ResponseEntity<StreamingResponseBody> getFile(final RawOperationContext context,
                                                       final HttpServletRequest request,
                                                       final String path)
    throws Exception {
    return groupRegistryHelper.findAnswer(context,
                                          (subContext, fileManager) -> {
                                            return fileManager.getFile(subContext, request, path);
                                          },
                                          String.format("File '%s' not found",
                                                        path));
  }
}
