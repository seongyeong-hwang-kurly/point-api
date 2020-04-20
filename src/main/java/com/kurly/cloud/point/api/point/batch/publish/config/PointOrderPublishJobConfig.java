package com.kurly.cloud.point.api.point.batch.publish.config;

import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.order.repository.OrderRepository;
import com.kurly.cloud.point.api.point.batch.publish.PointOrderPublishItemProcessor;
import com.kurly.cloud.point.api.point.batch.publish.PointOrderPublishItemReader;
import com.kurly.cloud.point.api.point.batch.publish.PointOrderPublishItemWriter;
import com.kurly.cloud.point.api.point.batch.publish.PointOrderPublishJobListener;
import com.kurly.cloud.point.api.point.domain.PublishPointRequest;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PointOrderPublishJobConfig {

  public static int CHUNK_SIZE = 1000;
  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

  private final StepBuilderFactory stepBuilderFactory;
  private final PointOrderPublishItemWriter pointOrderPublishItemWriter;
  private final OrderRepository orderRepository;

  @Value("${batch.publish.chunkSize:1000}")
  public void setChunkSize(int ChunkSize) {
    CHUNK_SIZE = ChunkSize;
  }

  @Bean
  public Job pointOrderPublishJob(JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("pointOrderPublishJob")
        .listener(new PointOrderPublishJobListener())
        .start(pointOrderPublishJobStep(stepBuilderFactory))
        .build();
  }

  public Step pointOrderPublishJobStep(StepBuilderFactory stepBuilderFactory) {
    return stepBuilderFactory.get("pointOrderPublishJobStep")
        .<Order, PublishPointRequest>chunk(CHUNK_SIZE)
        .reader(pointPublishOrderReader(null))
        .processor(new PointOrderPublishItemProcessor())
        .writer(pointOrderPublishItemWriter)
        .build();
  }

  @JobScope
  @Bean
  public AbstractPagingItemReader<Order> pointPublishOrderReader(
      @Value("#{jobParameters[publishDate]}") String publishDate) {
    return new PointOrderPublishItemReader(orderRepository, CHUNK_SIZE, publishDate);
  }
}

