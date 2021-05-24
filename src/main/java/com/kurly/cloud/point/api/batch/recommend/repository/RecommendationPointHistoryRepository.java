package com.kurly.cloud.point.api.batch.recommend.repository;

import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDataType;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDelayType;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointStatus;
import com.kurly.cloud.point.api.batch.recommend.entity.RecommendationPointHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationPointHistoryRepository
    extends JpaRepository<RecommendationPointHistory, Long> {

  Optional<RecommendationPointHistory> findFirstByOrderNumberAndDelayTypeAndType(
      long orderNumber,
      RecommendationDelayType delayType,
      RecommendationDataType type);

  List<RecommendationPointHistory> findAllByOrderMemberNumberAndStatus(
      long memberNumber,
      RecommendationPointStatus status
  );

  Optional<RecommendationPointHistory> findFirstByOrderNumberAndOrderMemberNumberAndStatus(
      long orderNumber,
      long memberNumber,
      RecommendationPointStatus status
  );

  @SuppressWarnings("checkstyle:LineLength")
  Optional<RecommendationPointHistory> findFirstByRecommendationMemberNumberOrderByCreateDateTimeDesc(
      long memberNumber
  );
}
