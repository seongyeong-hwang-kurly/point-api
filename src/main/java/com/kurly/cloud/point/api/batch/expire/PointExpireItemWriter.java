package com.kurly.cloud.point.api.batch.expire;

import com.kurly.cloud.point.api.batch.expire.config.PointExpireJobConfig;
import com.kurly.cloud.point.api.point.domain.PointExpireResult;
import com.kurly.cloud.point.api.point.port.in.ExpirePointPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;

public class PointExpireItemWriter implements ItemWriter<Long> {

  private final ExpirePointPort expirePointPort;
  private final ForkJoinPool forkJoinPool;
  private StepExecution stepExecution;
  private String expireTime;

  public PointExpireItemWriter(ExpirePointPort expirePointPort, int poolSize) {
    this.expirePointPort = expirePointPort;
    this.forkJoinPool = new ForkJoinPool(poolSize);
  }

  @Override public void write(List<? extends Long> items) throws Exception {
    forkJoinPool.submit(() -> {
      items.parallelStream().forEach(memberNumber -> {
        PointExpireResult result = expirePointPort.expireMemberPoint(memberNumber,
            LocalDateTime.parse(expireTime, PointExpireJobConfig.DATE_TIME_FORMATTER));
        putSummary(result);
      });
    }).get();
  }

  private synchronized void putSummary(PointExpireResult result) {
    ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
    long totalMemberCount = executionContext.getLong("totalMemberCount", 0);
    long totalExpiredPointAmount = executionContext.getLong("totalExpiredPointAmount", 0);
    long totalExpiredPointCount = executionContext.getLong("totalExpiredPointCount", 0);

    executionContext.putLong("totalMemberCount", totalMemberCount + 1);
    executionContext.putLong("totalExpiredPointCount",
        totalExpiredPointCount + result.getExpiredPointSeq().size());
    executionContext.putLong("totalExpiredPointAmount",
        totalExpiredPointAmount + result.getTotalExpired());
  }

  @BeforeStep
  public void saveStepExecution(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
    this.expireTime = stepExecution.getJobParameters().getString("expireTime");
  }
}
