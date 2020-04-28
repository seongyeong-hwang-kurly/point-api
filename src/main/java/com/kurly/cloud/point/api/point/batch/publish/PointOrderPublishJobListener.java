package com.kurly.cloud.point.api.point.batch.publish;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;

@Slf4j
public class PointOrderPublishJobListener implements JobExecutionListener {
  @Override public void beforeJob(JobExecution jobExecution) {
    log.debug("주문 적립금 적립 배치를 시작합니다");
  }

  @Override public void afterJob(JobExecution jobExecution) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Date startTime = jobExecution.getStartTime();
    Date endTime = jobExecution.getEndTime();
    long totalExecutionTimeInSeconds = (endTime.getTime() - startTime.getTime()) / 1000;
    log.debug("주문 적립금 적립 배치를 완료하였습니다");
    log.debug(MessageFormat.format("시작시간 : {0}", sdf.format(startTime)));
    log.debug(MessageFormat.format("종료시간 : {0}", sdf.format(endTime)));
    log.debug(MessageFormat.format("걸린시간 : {0}초", totalExecutionTimeInSeconds));

    ExecutionContext executionContext = jobExecution.getExecutionContext();
    long totalPublishCount = executionContext.getLong("totalPublishCount", 0);
    long totalPublishPointAmount = executionContext.getLong("totalPublishPointAmount", 0);

    log.debug(MessageFormat.format("적립 된 총 적립금 개수 : {0}", totalPublishCount));
    log.debug(MessageFormat.format("적립 된 총 적립금 수량 : {0}", totalPublishPointAmount));
  }
}
