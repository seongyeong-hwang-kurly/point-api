package com.kurly.cloud.point.api.batch.recommend;

import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.recommend.service.RecommendationPointHistoryService;
import org.springframework.batch.item.ItemProcessor;

public class RecommendPublishItemProcessor
    implements ItemProcessor<Order, RecommendationPointHistory> {

  RecommendationPointHistoryService recommendationPointHistoryService;

  public RecommendPublishItemProcessor(
      RecommendationPointHistoryService recommendationPointHistoryService) {
    this.recommendationPointHistoryService = recommendationPointHistoryService;
  }

  @Override public RecommendationPointHistory process(Order item) throws Exception {
    return recommendationPointHistoryService.generateByOrder(item).orElse(null);
  }
}
