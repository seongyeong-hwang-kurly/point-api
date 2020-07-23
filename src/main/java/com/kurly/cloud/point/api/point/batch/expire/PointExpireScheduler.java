package com.kurly.cloud.point.api.point.batch.expire;

import static com.kurly.cloud.point.api.point.batch.expire.config.PointExpireJobConfig.DATE_TIME_FORMATTER;

import com.kurly.cloud.api.common.util.SlackNotifier;
import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import java.time.LocalDateTime;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@ConditionalOnProperty("batch.enable")
@Component
@Slf4j
@RequiredArgsConstructor
public class PointExpireScheduler {
  @Qualifier("pointExpireJob")
  private final Job pointExpireJob;
  private final JobLauncher jobLauncher;

  /**
   * 스케줄 실행.
   */
  @Scheduled(cron = "0 0 0 * * *")
  public void execute() {
    String expireTime = LocalDateTime.now().format(DATE_TIME_FORMATTER);
    try {
      FileBeatLogger.info(new HashMap<>() {
        {
          put("batch", "PointExpireScheduler");
          put("action", "STARTED");
          put("param", expireTime);
        }
      });
      JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
      jobParametersBuilder.addString("expireTime", expireTime);
      JobExecution run = jobLauncher.run(pointExpireJob, jobParametersBuilder.toJobParameters());
      if (run.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode())) {
        String exitDescription = run.getExitStatus().getExitDescription();
        SlackNotifier.notify(exitDescription);
        FileBeatLogger.info(new HashMap<>() {
          {
            put("batch", "PointExpireScheduler");
            put("action", "FAILED");
            put("param", expireTime);
          }
        });
      } else {
        FileBeatLogger.info(new HashMap<>() {
          {
            put("batch", "PointExpireScheduler");
            put("action", "COMPLETED");
            put("param", expireTime);
          }
        });
      }
    } catch (Exception e) {
      FileBeatLogger.info(new HashMap<>() {
        {
          put("batch", "PointExpireScheduler");
          put("action", "FAILED");
          put("param", expireTime);
        }
      });
      FileBeatLogger.error(e);
      SlackNotifier.notify(e);
    }
  }
}
