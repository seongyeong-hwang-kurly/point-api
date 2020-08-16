package com.kurly.cloud.point.api.batch.recommend;

import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import com.kurly.cloud.point.api.recommend.domain.RecommendationPointStatus;
import com.kurly.cloud.point.api.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.recommend.service.RecommendationPointHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@StepScope
public class RecommendPublishItemWriter implements ItemWriter<RecommendationPointHistory> {

  private final PublishPointPort publishPointPort;
  private final RecommendationPointHistoryService recommendationPointHistoryService;
  private StepExecution stepExecution;

  @Override public void write(List<? extends RecommendationPointHistory> items) throws Exception {
    items.forEach(item -> {
      if (RecommendationPointStatus.PAID.equals(item.getStatus())) {
        //주문자 지급
        publishPointPort.publish(PublishPointRequest
            .builder()
            .orderNumber(item.getOrderNumber())
            .historyType(HistoryType.TYPE_29.getValue())
            .point(item.getPoint())
            .memberNumber(item.getOrderMemberNumber())
            .detail(item.getHistoryMsg())
            .build()
        );

        //추천인 지급
        publishPointPort.publish(PublishPointRequest
            .builder()
            .orderNumber(item.getOrderNumber())
            .historyType(HistoryType.TYPE_29.getValue())
            .point(item.getPoint())
            .memberNumber(item.getRecommendationMemberNumber())
            .detail(item.getRecommenderHistoryMsg())
            .build()
        );
      }
      recommendationPointHistoryService.save(item);
    });
  }

  @BeforeStep
  public void saveStepExecution(StepExecution stepExecution) {
    this.stepExecution = stepExecution;
  }
}
