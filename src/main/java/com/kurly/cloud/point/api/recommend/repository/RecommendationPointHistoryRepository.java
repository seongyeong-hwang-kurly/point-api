package com.kurly.cloud.point.api.recommend.repository;

import com.kurly.cloud.point.api.recommend.domain.RecommendationDataType;
import com.kurly.cloud.point.api.recommend.domain.RecommendationDelayType;
import com.kurly.cloud.point.api.recommend.domain.RecommendationPointStatus;
import com.kurly.cloud.point.api.recommend.entity.RecommendationPointHistory;
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

  Optional<RecommendationPointHistory>
  findFirstByRecommendationMemberNumberOrderByCreateDateTimeDesc(
      long memberNumber
  );
}
