package com.kurly.cloud.point.api.batch.recommend.service;

import com.kurly.cloud.point.api.batch.order.entity.Order;
import com.kurly.cloud.point.api.batch.recommend.entity.RecommendationPointHistory;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.lang.Nullable;

public interface RecommendationPointHistoryUseCase {
  void save(RecommendationPointHistory history);

  void setPromotionStartDate(String promotionStartDate);

  void setPromotionEndDate(String promotionEndDate);

  void setPromotionPaidPoint(int promotionPaidPoint);

  int getPaidPoint();

  int getPaidPoint(@Nullable LocalDateTime payDateTime);

  boolean isPromotionActive(LocalDateTime payDateTime);

  Optional<RecommendationPointHistory> generateByOrder(Order order);

  boolean isValidOrder(Order order);
}
