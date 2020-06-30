/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.batch.expire;

import com.kurly.cloud.point.api.point.batch.expire.config.PointExpireJobConfig;
import com.kurly.cloud.point.api.point.domain.PointExpireResult;
import com.kurly.cloud.point.api.point.port.in.ExpirePointPort;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@StepScope
public class PointExpireItemWriter implements ItemWriter<Long> {

  private final ExpirePointPort expirePointPort;
  private StepExecution stepExecution;
  private String expireTime;

  @Override public void write(List<? extends Long> items) throws Exception {
    items.parallelStream().forEach(memberNumber -> {
      PointExpireResult result = expirePointPort.expireMemberPoint(memberNumber,
          LocalDateTime.parse(expireTime, PointExpireJobConfig.DATE_TIME_FORMATTER));

      ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
      long totalMemberCount = executionContext.getLong("totalMemberCount", 0);
      long totalExpiredPointAmount = executionContext.getLong("totalExpiredPointAmount", 0);
      long totalExpiredPointCount = executionContext.getLong("totalExpiredPointCount", 0);

      executionContext.putLong("totalMemberCount", totalMemberCount + 1);
      executionContext.putLong("totalExpiredPointCount",
          totalExpiredPointCount + result.getExpiredPointSeq().size());
      executionContext.putLong("totalExpiredPointAmount",
          totalExpiredPointAmount + result.getTotalExpired());
    });
  }

  @BeforeStep
  public void saveStepExecution(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
    this.expireTime = stepExecution.getJobParameters().getString("expireTime");
  }
}
