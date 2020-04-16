package com.kurly.cloud.point.api.point.batch.config;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@EnableScheduling
public class PointBatchConfig extends DefaultBatchConfigurer {

  @Override protected JobRepository createJobRepository() throws Exception {
    MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
    factory.setTransactionManager(getTransactionManager());
    factory.setValidateTransactionState(false);
    return factory.getObject();
  }

  @Bean("batchTransactionManager")
  @Override public PlatformTransactionManager getTransactionManager() {
    return new ResourcelessTransactionManager();
  }

}

