package com.kurly.cloud.point.api.point.batch.expire.config;

import com.kurly.cloud.point.api.point.batch.expire.PointExpireItemReader;
import com.kurly.cloud.point.api.point.batch.expire.PointExpireItemWriter;
import com.kurly.cloud.point.api.point.batch.expire.PointExpireJobListener;
import com.kurly.cloud.point.api.point.port.in.ExpirePointPort;
import com.kurly.cloud.point.api.point.repository.PointRepository;
import com.kurly.cloud.point.api.point.util.SlackBot;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PointExpireJobConfig {

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
  private int chunkSize = 10_000;
  private int poolSize = 10;
  private final StepBuilderFactory stepBuilderFactory;
  private final ExpirePointPort expirePointPort;
  private final PointRepository pointRepository;
  private final SlackBot slackBot;

  @Value("${batch.expire.chunkSize:10000}")
  public void setChunkSize(int chunkSize) {
    this.chunkSize = chunkSize;
  }

  @Value("${batch.expire.poolSize:10}")
  public void setPoolSize(int poolSize) {
    this.poolSize = poolSize;
  }

  @Bean
  Job pointExpireJob(JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("pointExpireJob")
        .listener(new PointExpireJobListener(slackBot))
        .start(pointExpireJobStep(stepBuilderFactory))
        .build();
  }

  Step pointExpireJobStep(StepBuilderFactory stepBuilderFactory) {
    return stepBuilderFactory.get("pointExpireJobStep")
        .<Long, Long>chunk(chunkSize)
        .reader(expirePointMemberNumberReader(null))
        .writer(pointExpireItemWriter())
        .build();
  }

  public ItemWriter<Long> pointExpireItemWriter() {
    return new PointExpireItemWriter(expirePointPort, poolSize);
  }

  @JobScope
  @Bean
  public AbstractPagingItemReader<Long> expirePointMemberNumberReader(
      @Value("#{jobParameters[expireTime]}") String expireTime) {
    return new PointExpireItemReader(pointRepository, chunkSize, expireTime);
  }
}

