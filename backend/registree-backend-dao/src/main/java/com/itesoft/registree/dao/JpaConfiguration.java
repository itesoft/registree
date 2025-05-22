package com.itesoft.registree.dao;

import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(value = "com.itesoft.registree",
                       excludeFilters = @Filter(type = FilterType.REGEX, pattern = "com.itesoft.registree.dao.RegistreeRepository"))
@EnableTransactionManagement
public class JpaConfiguration {
}
