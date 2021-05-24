package com.kurly.cloud.point.api.batch.recommend;

import com.kurly.cloud.point.api.batch.order.entity.Order;
import com.kurly.cloud.point.api.batch.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.batch.recommend.service.RecommendationPointHistoryUseCase;
import org.springframework.batch.item.ItemProcessor;

public class RecommendPublishItemProcessor
    implements ItemProcessor<Order, RecommendationPointHistory> {

  RecommendationPointHistoryUseCase recommendationPointHistoryUseCase;

  public RecommendPublishItemProcessor(
      RecommendationPointHistoryUseCase recommendationPointHistoryUseCase) {
    this.recommendationPointHistoryUseCase = recommendationPointHistoryUseCase;
  }

  @Override
  public RecommendationPointHistory process(Order item) throws Exception {
    return recommendationPointHistoryUseCase.generateByOrder(item).orElse(null);
  }
}
