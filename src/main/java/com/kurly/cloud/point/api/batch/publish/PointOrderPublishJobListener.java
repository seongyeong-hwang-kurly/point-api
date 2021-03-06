package com.kurly.cloud.point.api.batch.publish;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import com.kurly.cloud.point.api.point.util.SlackBot;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;

@Slf4j
public class PointOrderPublishJobListener implements JobExecutionListener {
  private final SlackBot slackBot;

  public PointOrderPublishJobListener(SlackBot slackBot) {
    this.slackBot = slackBot;
  }

  @Override public void beforeJob(JobExecution jobExecution) {
    log.debug("주문 적립금 적립 배치를 시작합니다");
  }

  @Override public void afterJob(JobExecution jobExecution) {
    Date startTime = jobExecution.getStartTime();
    Date endTime = jobExecution.getEndTime();
    ExecutionContext executionContext = jobExecution.getExecutionContext();
    long totalPublishCount = executionContext.getLong("totalPublishCount", 0);
    long totalPublishPointAmount = executionContext.getLong("totalPublishPointAmount", 0);

    doLog(jobExecution.getExitStatus(), startTime, endTime, totalPublishCount,
        totalPublishPointAmount);
  }

  private void doLog(ExitStatus exitStatus, Date startTime,
                     Date endTime, long totalPublishCount,
                     long totalPublishPointAmount) {
    long totalExecutionTimeInSeconds = (endTime.getTime() - startTime.getTime()) / 1000;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    List<String> messages = Arrays.asList(
        "*주문 적립금 적립 배치를 완료하였습니다*",
        MessageFormat.format("실행결과 : {0}", exitStatus.getExitCode()),
        MessageFormat.format("시작시간 : {0}", sdf.format(startTime)),
        MessageFormat.format("종료시간 : {0}", sdf.format(endTime)),
        MessageFormat.format("걸린시간 : {0}초", totalExecutionTimeInSeconds),
        MessageFormat.format("적립 된 총 적립금 개수 : {0}", totalPublishCount),
        MessageFormat.format("적립 된 총 적립금 수량 : {0}", totalPublishPointAmount),
        MessageFormat.format("적용 만료일 : {0}", PointExpireDateCalculator.calculateDefault(
            LocalDateTime.now()).format(PointBatchConfig.DATE_TIME_FORMATTER))
    );

    messages.forEach(log::debug);
    slackBot.postMessage(String.join("\n", messages));

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "pointPublishedSummary");
        put("amount", totalPublishPointAmount);
        put("count", totalPublishCount);
        put("totalExecutionTimeInSeconds", totalExecutionTimeInSeconds);
      }
    });
  }
}
