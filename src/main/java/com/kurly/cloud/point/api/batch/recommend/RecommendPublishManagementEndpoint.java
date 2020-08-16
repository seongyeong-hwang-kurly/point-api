package com.kurly.cloud.point.api.batch.recommend;

import com.kurly.cloud.api.common.util.SlackNotifier;
import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@ConditionalOnProperty("batch.recommend.enable")
@RequiredArgsConstructor
@Endpoint(id = "recommendPublish")
public class RecommendPublishManagementEndpoint {

  private final JobLauncher jobLauncher;
  @Qualifier("recommendPublishJob")
  private final Job recommendPublishJob;

  @WriteOperation
  String executeRecommendPublishBatch(@Nullable String deliveredDate) {
    if (StringUtils.isEmpty(deliveredDate)) {
      deliveredDate =
          LocalDateTime.now().minusDays(1).format(PointBatchConfig.DATE_TIME_FORMATTER);
    }
    JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
    jobParametersBuilder.addDate("now", new Date());
    jobParametersBuilder.addString("deliveredDate", deliveredDate);
    new Thread(() -> {
      try {
        jobLauncher.run(recommendPublishJob, jobParametersBuilder.toJobParameters());
      } catch (Exception e) {
        SlackNotifier.notify(e);
        FileBeatLogger.error(e);
      }
    }).start();
    return "ok";
  }
}
