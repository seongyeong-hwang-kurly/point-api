package com.kurly.cloud.point.api.point.batch.controller;

import com.kurly.cloud.api.common.util.SlackNotifier;
import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.batch.expire.config.PointExpireJobConfig;
import com.kurly.cloud.point.api.point.batch.publish.config.PointOrderPublishJobConfig;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@ConditionalOnProperty("batch.enable")
@RequiredArgsConstructor
@RestController
public class PointBatchController {

  private final JobLauncher jobLauncher;
  @Qualifier("pointExpireJob")
  private final Job pointExpireJob;
  @Qualifier("pointOrderPublishJob")
  private final Job pointOrderPublishJob;

  @GetMapping({"/batch/execute/expire/{expireTime}", "/batch/execute/expire/"})
  void executeExpireBatch(@PathVariable(required = false) String expireTime) {
    if (StringUtils.isEmpty(expireTime)) {
      expireTime = LocalDateTime.now().format(PointExpireJobConfig.DATE_TIME_FORMATTER);
    }
    JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
    jobParametersBuilder.addDate("now", new Date());
    jobParametersBuilder.addString("expireTime", expireTime);
    new Thread(() -> {
      try {
        jobLauncher.run(pointExpireJob, jobParametersBuilder.toJobParameters());
      } catch (Exception e) {
        SlackNotifier.notify(e);
        FileBeatLogger.error(e);
      }
    }).start();
  }

  @GetMapping({"/batch/execute/order-publish/{publishDate}", "/batch/execute/order-publish/"})
  void executeOrderPublishBatch(@PathVariable(required = false) String publishDate) {
    if (StringUtils.isEmpty(publishDate)) {
      publishDate = LocalDateTime.now().format(PointOrderPublishJobConfig.DATE_TIME_FORMATTER);
    }
    JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
    jobParametersBuilder.addDate("now", new Date());
    jobParametersBuilder.addString("publishDate", publishDate);
    new Thread(() -> {
      try {
        jobLauncher.run(pointOrderPublishJob, jobParametersBuilder.toJobParameters());
      } catch (Exception e) {
        SlackNotifier.notify(e);
        FileBeatLogger.error(e);
      }
    }).start();
  }
}
