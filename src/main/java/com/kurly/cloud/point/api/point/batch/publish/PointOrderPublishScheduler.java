/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.batch.publish;

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
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static com.kurly.cloud.point.api.point.batch.expire.config.PointExpireJobConfig.DATE_TIME_FORMATTER;

@Profile("enable-batch")
@Component
@Slf4j
@RequiredArgsConstructor
public class PointOrderPublishScheduler {
  @Qualifier("pointOrderPublishJob")
  private final Job pointOrderPublishJob;
  private final JobLauncher jobLauncher;

  @Scheduled(cron = "0 0 7 * * *")
  public void execute() {
    String publishDate = LocalDateTime.now().format(DATE_TIME_FORMATTER);
    try {
      FileBeatLogger.info(new HashMap<>() {{
        put("batch", "PointOrderPublishScheduler");
        put("action", "STARTED");
        put("param", publishDate);
      }});
      JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
      jobParametersBuilder.addString("publishDate", publishDate);
      JobExecution run = jobLauncher.run(pointOrderPublishJob, jobParametersBuilder.toJobParameters());
      if (run.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode())) {
        String exitDescription = run.getExitStatus().getExitDescription();
        SlackNotifier.notify(exitDescription);
        FileBeatLogger.info(new HashMap<>() {{
          put("batch", "PointOrderPublishScheduler");
          put("action", "FAILED");
          put("param", publishDate);
        }});
      } else {
        FileBeatLogger.info(new HashMap<>() {{
          put("batch", "PointOrderPublishScheduler");
          put("action", "COMPLETED");
          put("param", publishDate);
        }});
      }
    } catch (Exception e) {
      FileBeatLogger.info(new HashMap<>() {{
        put("batch", "PointOrderPublishScheduler");
        put("action", "FAILED");
        put("param", publishDate);
      }});
      FileBeatLogger.error(e);
      SlackNotifier.notify(e);
    }
  }
}
