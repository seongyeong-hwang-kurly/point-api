package com.kurly.cloud.point.api.batch.recommend.config;

import com.kurly.cloud.point.api.batch.recommend.RecommendPublishItemProcessor;
import com.kurly.cloud.point.api.batch.recommend.RecommendPublishItemReader;
import com.kurly.cloud.point.api.batch.recommend.RecommendPublishItemWriter;
import com.kurly.cloud.point.api.batch.recommend.RecommendPublishJobListener;
import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.point.util.SlackBot;
import com.kurly.cloud.point.api.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.recommend.service.RecommendationPointHistoryService;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RecommendPublishJobConfig {
  public static int CHUNK_SIZE = 1000;
  private final StepBuilderFactory stepBuilderFactory;
  private final SlackBot slackBot;
  private final EntityManagerFactory entityManagerFactory;
  private final RecommendationPointHistoryService recommendationPointHistoryService;
  private final RecommendPublishItemWriter recommendPublishItemWriter;

  @Value("${batch.recommend.chunkSize:1000}")
  public void setChunkSize(int chunkSize) {
    CHUNK_SIZE = chunkSize;
  }

  @Bean
  Job recommendPublishJob(JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("recommendPublishJob")
        .listener(new RecommendPublishJobListener(slackBot))
        .start(recommendPublishJobStep(stepBuilderFactory))
        .build();
  }

  Step recommendPublishJobStep(StepBuilderFactory stepBuilderFactory) {
    return stepBuilderFactory.get("recommendPublishJobStep")
        .<Order, RecommendationPointHistory>chunk(CHUNK_SIZE)
        .reader(recommendPublishItemReader(null))
        .processor(new RecommendPublishItemProcessor(recommendationPointHistoryService))
        .writer(recommendPublishItemWriter)
        .build();
  }

  @JobScope
  @Bean
  public JpaPagingItemReader<Order> recommendPublishItemReader(
      @Value("#{jobParameters[deliveredDate]}") String deliveredDate) {
    return new RecommendPublishItemReader(entityManagerFactory, CHUNK_SIZE, deliveredDate);
  }

}
