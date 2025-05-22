package com.itesoft.registree.registry.api.listener;

import com.itesoft.registree.dto.File;

public interface FileOperationListener {
  void fileCreated(File file);

  void fileUpdated(File oldFile, File newFile);

  void fileDeleting(File file);
}
