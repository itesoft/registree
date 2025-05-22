package com.itesoft.registree.dao.jpa;

import java.util.Optional;

import com.itesoft.registree.dao.RegistreeRepository;

import org.springframework.data.jpa.repository.Query;

public interface FileRepository extends RegistreeRepository<FileEntity, String> {
  @Query(value = "SELECT 1 FROM reg_file f WHERE f.id=?1 FOR UPDATE", nativeQuery = true)
  void lock(String id);

  @Query(value = "SELECT 1 FROM reg_file f WHERE f.registry_name=?1 AND f.path=?2 FOR UPDATE", nativeQuery = true)
  void lock(String registryName, String path);

  boolean existsByRegistryNameAndPath(String registryName, String path);

  boolean existsByRegistryNameAndPathAndIdNot(String registryName, String path, String id);

  Optional<FileEntity> findByRegistryNameAndPath(String registryName, String path);

  void deleteAllByComponentId(String componentId);
}
