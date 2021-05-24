package com.kurly.cloud.point.api.batch.recommend.config;

import com.kurly.cloud.point.api.batch.order.entity.Order;
import com.kurly.cloud.point.api.batch.recommend.RecommendPublishItemProcessor;
import com.kurly.cloud.point.api.batch.recommend.RecommendPublishItemReader;
import com.kurly.cloud.point.api.batch.recommend.RecommendPublishItemWriter;
import com.kurly.cloud.point.api.batch.recommend.RecommendPublishJobListener;
import com.kurly.cloud.point.api.batch.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.batch.recommend.service.RecommendationPointHistoryUseCase;
import com.kurly.cloud.point.api.point.util.SlackBot;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class RecommendPublishJobConfig {
  private int chunkSize = 100;
  private int poolSize = 10;
  private final StepBuilderFactory stepBuilderFactory;
  private final SlackBot slackBot;
  private final EntityManagerFactory entityManagerFactory;
  private final RecommendationPointHistoryUseCase recommendationPointHistoryUseCase;
  private final RecommendPublishItemWriter recommendPublishItemWriter;

  @Value("${batch.recommend.chunkSize:100}")
  public void setChunkSize(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  @Value("${batch.recommend.poolSize:10}")
  public void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
  }

  TaskExecutor executor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(poolSize);
    executor.setMaxPoolSize(poolSize);
    executor.setThreadNamePrefix("recommend-publish-");
    executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
    executor.initialize();
    return executor;
  }

  @Bean
  Job recommendPublishJob(JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("recommendPublishJob")
        .listener(new RecommendPublishJobListener(slackBot))
        .start(recommendPublishJobStep(stepBuilderFactory))
        .build();
  }

  @JobScope
  Step recommendPublishJobStep(StepBuilderFactory stepBuilderFactory) {
    return stepBuilderFactory.get("recommendPublishJobStep")
        .<Order, RecommendationPointHistory>chunk(chunkSize)
        .reader(recommendPublishItemReader(null))
        .processor(new RecommendPublishItemProcessor(recommendationPointHistoryUseCase))
        .writer(recommendPublishItemWriter)
        .taskExecutor(executor())
        .throttleLimit(poolSize)
        .build();
  }

  @StepScope
  @Bean
  public JpaPagingItemReader<Order> recommendPublishItemReader(
      @Value("#{jobParameters[deliveredDate]}") String deliveredDate) {
    return new RecommendPublishItemReader(entityManagerFactory, chunkSize, deliveredDate);
  }

}
