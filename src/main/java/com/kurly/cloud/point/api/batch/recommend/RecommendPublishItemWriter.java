package com.kurly.cloud.point.api.batch.recommend;

import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointStatus;
import com.kurly.cloud.point.api.batch.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.batch.recommend.service.RecommendationPointHistoryUseCase;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
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
public class RecommendPublishItemWriter implements ItemWriter<RecommendationPointHistory> {

  private final PublishPointUseCase publishPointUseCase;
  private final RecommendationPointHistoryUseCase recommendationPointHistoryUseCase;
  private StepExecution stepExecution;

  @Override public void write(List<? extends RecommendationPointHistory> items) throws Exception {
    items.forEach(item -> {
      if (RecommendationPointStatus.PAID.equals(item.getStatus())) {
        //주문자 지급
        publishPointUseCase.publish(PublishPointRequest
            .builder()
            .orderNumber(item.getOrderNumber())
            .historyType(HistoryType.TYPE_12.getValue())
            .point(item.getPoint())
            .memberNumber(item.getOrderMemberNumber())
            .detail(HistoryType.TYPE_12.buildMessage())
            .build()
        );

        //추천인 지급
        publishPointUseCase.publish(PublishPointRequest
            .builder()
            .orderNumber(item.getOrderNumber())
            .historyType(HistoryType.TYPE_13.getValue())
            .point(item.getPoint())
            .memberNumber(item.getRecommendationMemberNumber())
            .detail(HistoryType.TYPE_13.buildMessage(item.getMaskedName(item.getOrderMemberName())))
            .build()
        );
      }
      putSummary(item);
      recommendationPointHistoryUseCase.save(item);
    });
  }

  private synchronized void putSummary(RecommendationPointHistory result) {
    ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
    long totalOrderCount = executionContext.getLong("totalOrderCount", 0);
    long totalValidCount = executionContext.getLong("totalValidCount", 0);
    long totalPublishPointAmount = executionContext.getLong("totalPublishPointAmount", 0);
    long totalPublishPointCount = executionContext.getLong("totalPublishPointCount", 0);

    executionContext.putLong("totalOrderCount", totalOrderCount + 1);
    if (RecommendationPointStatus.PAID.equals(result.getStatus())) {
      executionContext.putLong("totalValidCount", totalValidCount + 1);
      executionContext
          .putLong("totalPublishPointAmount", totalPublishPointAmount + result.getPoint() * 2);
      executionContext
          .putLong("totalPublishPointCount", totalPublishPointCount + 2);
    }
  }

  @BeforeStep
  public void saveStepExecution(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
  }
}
