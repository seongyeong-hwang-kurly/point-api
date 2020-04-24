/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

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
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Profile("enable-batch")
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
