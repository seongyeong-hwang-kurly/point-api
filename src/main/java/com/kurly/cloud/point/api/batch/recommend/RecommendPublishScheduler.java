package com.kurly.cloud.point.api.batch.recommend;

import com.kurly.cloud.api.common.util.SlackNotifier;
import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
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

@ConditionalOnProperty("batch.recommend.enable")
@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendPublishScheduler {
  @Qualifier("recommendPublishJob")
  private final Job recommendPublishJob;
  private final JobLauncher jobLauncher;

  /**
   * 스케쥴 실행.
   */
  @Scheduled(cron = "0 0 8 * * *")
  public void execute() {
    String deliveredDate =
        LocalDateTime.now().minusDays(1).format(PointBatchConfig.DATE_TIME_FORMATTER);
    try {
      FileBeatLogger.info(new HashMap<>() {
        {
          put("batch", "RecommendPublishScheduler");
          put("action", "STARTED");
          put("param", deliveredDate);
        }
      });
      JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
      jobParametersBuilder.addString("deliveredDate", deliveredDate);
      JobExecution run =
          jobLauncher.run(recommendPublishJob, jobParametersBuilder.toJobParameters());

      if (run.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode())) {
        String exitDescription = run.getExitStatus().getExitDescription();
        SlackNotifier.notify(exitDescription);
        FileBeatLogger.info(new HashMap<>() {
          {
            put("batch", "RecommendPublishScheduler");
            put("action", "FAILED");
            put("param", deliveredDate);
          }
        });
      } else {
        FileBeatLogger.info(new HashMap<>() {
          {
            put("batch", "RecommendPublishScheduler");
            put("action", "COMPLETED");
            put("param", deliveredDate);
          }
        });
      }
    } catch (Exception e) {
      FileBeatLogger.info(new HashMap<>() {
        {
          put("batch", "RecommendPublishScheduler");
          put("action", "FAILED");
          put("param", deliveredDate);
        }
      });
      FileBeatLogger.error(e);
      SlackNotifier.notify(e);
    }
  }
}
