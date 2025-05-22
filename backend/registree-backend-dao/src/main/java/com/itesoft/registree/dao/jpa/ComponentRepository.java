package com.itesoft.registree.dao.jpa;

import java.util.Optional;

import com.itesoft.registree.dao.RegistreeRepository;

import org.springframework.data.jpa.repository.Query;

public interface ComponentRepository extends RegistreeRepository<ComponentEntity, String> {
  @Query(value = "SELECT 1 FROM reg_component c WHERE c.id=?1 FOR UPDATE", nativeQuery = true)
  void lock(String id);

  @Query(value = "SELECT 1 FROM reg_component c WHERE c.registry_name=?1 AND c.group_name IS NULL AND c.name=?2 AND c.version=?3 FOR UPDATE",
         nativeQuery = true)
  void lock(String registryName,
            String name,
            String version);

  @Query(value = "SELECT 1 FROM reg_component c WHERE c.registry_name=?1 AND c.group_name=?2 AND c.name=?3 AND c.version=?4 FOR UPDATE", nativeQuery = true)
  void lock(String registryName,
            String group,
            String name,
            String version);

  boolean existsByRegistryNameAndGroupAndNameAndVersion(String registryName,
                                                        String group,
                                                        String name,
                                                        String version);

  boolean existsByRegistryNameAndGroupIsNullAndNameAndVersion(String registryName,
                                                              String name,
                                                              String version);

  boolean existsByRegistryNameAndGroupAndNameAndVersionAndIdNot(String registryName,
                                                                String group,
                                                                String name,
                                                                String version,
                                                                String id);

  boolean existsByRegistryNameAndGroupIsNullAndNameAndVersionAndIdNot(String registryName,
                                                                      String name,
                                                                      String version,
                                                                      String id);

  Optional<ComponentEntity> findByRegistryNameAndGroupAndNameAndVersion(String registryName,
                                                                        String group,
                                                                        String name,
                                                                        String version);

  Optional<ComponentEntity> findByRegistryNameAndGroupIsNullAndNameAndVersion(String registryName,
                                                                              String name,
                                                                              String version);
}
