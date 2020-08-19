package com.kurly.cloud.point.api.batch.config;

import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.boot.task.TaskSchedulerBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableBatchProcessing
@EnableScheduling
@RequiredArgsConstructor
public class PointBatchConfig extends DefaultBatchConfigurer implements SchedulingConfigurer {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  private static final int SCHEDULER_POOL_SIZE = 5;

  @Override protected JobRepository createJobRepository() throws Exception {
    MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean();
    return factory.getObject();
  }

  @Override public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    ThreadPoolTaskScheduler taskScheduler = new TaskSchedulerBuilder()
        .poolSize(SCHEDULER_POOL_SIZE)
        .build();
    taskScheduler.initialize();
    taskRegistrar.setTaskScheduler(taskScheduler);
  }
}

