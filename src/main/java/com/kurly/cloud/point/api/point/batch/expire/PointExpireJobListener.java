/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.batch.expire;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.item.ExecutionContext;

@Slf4j
public class PointExpireJobListener implements JobExecutionListener {

  public void beforeJob(JobExecution jobExecution) {
    log.debug("적립금 만료 배치를 시작합니다");
  }

  public void afterJob(JobExecution jobExecution) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Date startTime = jobExecution.getStartTime();
    Date endTime = jobExecution.getEndTime();
    long totalExecutionTimeInSeconds = (endTime.getTime() - startTime.getTime()) / 1000;
    log.debug("적립금 만료 배치를 완료하였습니다");
    log.debug(MessageFormat.format("시작시간 : {0}", sdf.format(startTime)));
    log.debug(MessageFormat.format("종료시간 : {0}", sdf.format(endTime)));
    log.debug(MessageFormat.format("걸린시간 : {0}초", totalExecutionTimeInSeconds));

    ExecutionContext executionContext = jobExecution.getExecutionContext();
    long totalMemberCount = executionContext.getLong("totalMemberCount", 0);
    long totalExpiredPointCount = executionContext.getLong("totalExpiredPointCount", 0);
    long totalExpiredPointAmount = executionContext.getLong("totalExpiredPointAmount", 0);

    log.debug(MessageFormat.format("만료 처리 된 총 회원 수 : {0}", totalMemberCount));
    log.debug(MessageFormat.format("만료 처리 된 총 적립금 수 : {0}", totalExpiredPointCount));
    log.debug(MessageFormat.format("만료 처리 된 총 적립금 : {0}", totalExpiredPointAmount));

    FileBeatLogger.info(new HashMap<>() {{
      put("action", "pointExpiredSummary");
      put("expiredMemberCount", totalMemberCount);
      put("expiredAmount", totalExpiredPointAmount);
      put("expiredPointCount", totalExpiredPointCount);
      put("totalExecutionTimeInSeconds", totalExecutionTimeInSeconds);
    }});
  }

}
