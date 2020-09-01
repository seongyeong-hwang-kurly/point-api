package com.kurly.cloud.point.api.batch.publish.config;

import com.kurly.cloud.point.api.batch.publish.PointOrderPublishItemProcessor;
import com.kurly.cloud.point.api.batch.publish.PointOrderPublishItemReader;
import com.kurly.cloud.point.api.batch.publish.PointOrderPublishItemWriter;
import com.kurly.cloud.point.api.batch.publish.PointOrderPublishJobListener;
import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.util.SlackBot;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class PointOrderPublishJobConfig {

  private int chunkSize = 100;
  private int poolSize = 10;
  private final StepBuilderFactory stepBuilderFactory;
  private final PointOrderPublishItemWriter pointOrderPublishItemWriter;
  private final EntityManagerFactory entityManagerFactory;
  private final SlackBot slackBot;

  @Value("${batch.publish.chunkSize:100}")
  public void setChunkSize(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  @Value("${batch.publish.poolSize:10}")
  public void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
  }

  TaskExecutor executor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(poolSize);
    executor.setMaxPoolSize(poolSize);
    executor.setThreadNamePrefix("order-publish-");
    executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
    executor.initialize();
    return executor;
  }

  @Bean
  Job pointOrderPublishJob(JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("pointOrderPublishJob")
        .listener(new PointOrderPublishJobListener(slackBot))
        .start(pointOrderPublishJobStep(stepBuilderFactory))
        .build();
  }

  @JobScope
  Step pointOrderPublishJobStep(StepBuilderFactory stepBuilderFactory) {
    return stepBuilderFactory.get("pointOrderPublishJobStep")
        .<Order, PublishPointRequest>chunk(chunkSize)
        .reader(pointPublishOrderReader(null))
        .processor(new PointOrderPublishItemProcessor())
        .writer(pointOrderPublishItemWriter)
        .taskExecutor(executor())
        .throttleLimit(poolSize)
        .build();
  }

  @StepScope
  @Bean
  public AbstractPagingItemReader<Order> pointPublishOrderReader(
      @Value("#{jobParameters[publishDate]}") String publishDate) {
    return new PointOrderPublishItemReader(entityManagerFactory, chunkSize, publishDate);
  }
}

