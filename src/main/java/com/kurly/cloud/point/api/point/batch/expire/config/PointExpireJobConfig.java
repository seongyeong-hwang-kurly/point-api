package com.kurly.cloud.point.api.point.batch.expire.config;

import com.kurly.cloud.point.api.point.batch.expire.PointExpireItemReader;
import com.kurly.cloud.point.api.point.batch.expire.PointExpireItemWriter;
import com.kurly.cloud.point.api.point.batch.expire.PointExpireJobListener;
import com.kurly.cloud.point.api.point.repository.PointRepository;
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
public class PointExpireJobConfig {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  public static int CHUNK_SIZE = 1000;
  private final StepBuilderFactory stepBuilderFactory;
  private final PointExpireItemWriter pointExpireItemWriter;
  private final PointRepository pointRepository;

  @Value("${batch.expire.chunkSize:1000}")
  public void setChunkSize(int ChunkSize) {
    CHUNK_SIZE = ChunkSize;
  }

  @Bean
  public Job pointExpireJob(JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("pointExpireJob")
        .listener(new PointExpireJobListener())
        .start(pointExpireJobStep(stepBuilderFactory))
        .build();
  }

  public Step pointExpireJobStep(StepBuilderFactory stepBuilderFactory) {
    return stepBuilderFactory.get("pointExpireJobStep")
        .<Long, Long>chunk(CHUNK_SIZE)
        .reader(expirePointMemberNumberReader(null))
        .writer(pointExpireItemWriter)
        .build();
  }

  @JobScope
  @Bean
  public AbstractPagingItemReader<Long> expirePointMemberNumberReader(
      @Value("#{jobParameters[expireTime]}") String expireTime) {
    return new PointExpireItemReader(pointRepository, CHUNK_SIZE, expireTime);
  }
}

