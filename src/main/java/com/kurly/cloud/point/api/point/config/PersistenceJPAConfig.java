package com.kurly.cloud.point.api.point.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PersistenceJPAConfig {
  @Bean
  @Primary
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }
}
