package com.itesoft.registree.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RegistreeRepository<T, ID>
    extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
}
