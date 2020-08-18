package com.kurly.cloud.point.api.batch.recommend;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.util.SlackBot;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;

@Slf4j
public class RecommendPublishJobListener implements JobExecutionListener {

  private final SlackBot slackBot;

  public RecommendPublishJobListener(SlackBot slackBot) {
    this.slackBot = slackBot;
  }

  public void beforeJob(JobExecution jobExecution) {
    log.debug("친구 초대 적립금 적립 배치를 시작합니다");
  }

  /**
   * 작업 수행 후 요약 로그를 출력 한다.
   */
  public void afterJob(JobExecution jobExecution) {
    Date startTime = jobExecution.getStartTime();
    Date endTime = jobExecution.getEndTime();
    ExecutionContext executionContext = jobExecution.getExecutionContext();
    long totalOrderCount = executionContext.getLong("totalOrderCount", 0);
    long totalValidCount = executionContext.getLong("totalValidCount", 0);
    long totalPublishPointAmount = executionContext.getLong("totalPublishPointAmount", 0);
    long totalPublishPointCount = executionContext.getLong("totalPublishPointCount", 0);

    doLog(startTime, endTime, totalOrderCount, totalValidCount, totalPublishPointAmount,
        totalPublishPointCount);

  }

  void doLog(Date startTime, Date endTime, long totalOrderCount, long totalValidCount,
             long totalPublishPointAmount, long totalPublishPointCount) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    long totalExecutionTimeInSeconds = (endTime.getTime() - startTime.getTime()) / 1000;
    List<String> messages = Arrays.asList(
        "*친구 초대 적립금 적립 배치를 완료하였습니다*",
        MessageFormat.format("시작시간 : {0}", sdf.format(startTime)),
        MessageFormat.format("종료시간 : {0}", sdf.format(endTime)),
        MessageFormat.format("걸린시간 : {0}초", totalExecutionTimeInSeconds),
        MessageFormat.format("검사 한 총 주문 수 : {0}", totalOrderCount),
        MessageFormat.format("지급 대상 총 주문 수 : {0}", totalValidCount),
        MessageFormat.format("지급 된 총 적립금 수 : {0}", totalPublishPointCount),
        MessageFormat.format("지급 된 총 적립금 : {0}", totalPublishPointAmount)
    );

    messages.forEach(log::debug);
    slackBot.postMessage(String.join("\n", messages));

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "recommendPublishSummary");
        put("totalOrderCount", totalOrderCount);
        put("totalValidCount", totalValidCount);
        put("totalPublishPointAmount", totalPublishPointAmount);
        put("totalPublishPointCount", totalPublishPointCount);
        put("totalExecutionTimeInSeconds", totalExecutionTimeInSeconds);
      }
    });
  }
}
