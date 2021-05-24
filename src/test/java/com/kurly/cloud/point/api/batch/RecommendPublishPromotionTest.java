package com.kurly.cloud.point.api.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.kurly.cloud.point.api.batch.recommend.service.RecommendationPointHistoryUseCase;
import com.kurly.cloud.point.api.batch.recommend.service.impl.RecommendationPointHistoryService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


@DisplayName("프로모션 지급 기능 테스트")
public class RecommendPublishPromotionTest {

  final int NORMAL_PAID_POINT = 5000;
  final int PROMOTION_PAID_POINT = 10000;

  @DisplayName("설정값이 입력되지 않을 때")
  @Nested
  class DescribeHasNoConfig {

    RecommendationPointHistoryUseCase subject() {
      return new RecommendationPointHistoryService(null, null, null, null);
    }

    @DisplayName("아무런 값이 입력되지 않으면 기본 적립금을 반환한다.")
    @Test
    void no_config() {
      assertThat(subject().getPaidPoint()).isEqualTo(NORMAL_PAID_POINT);
    }

  }

  @DisplayName("설정값이 입력되었을 때")
  @Nested
  class DescribeHasConfig {

    RecommendationPointHistoryUseCase subject() {
      RecommendationPointHistoryService subject =
          new RecommendationPointHistoryService(null, null, null, null);
      subject.setPromotionStartDate("2021-03-01");
      subject.setPromotionEndDate("2021-03-03");
      subject.setPromotionPaidPoint(PROMOTION_PAID_POINT);
      return subject;
    }

    @DisplayName("주문일이 없으면 기본 적립금을 반환한다.")
    @Test
    void no_pay_date() {
      RecommendationPointHistoryUseCase subject = subject();
      assertThat(subject.getPaidPoint()).isEqualTo(NORMAL_PAID_POINT);
    }

    @DisplayName("주문일이 프로모션 시작일 이전이면 기본 적립금을 반환한다.")
    @Test
    void paydate_is_startdate_before() {
      RecommendationPointHistoryUseCase subject = subject();
      assertThat(subject.getPaidPoint(LocalDateTime.of(2021, 2, 28, 23, 0, 0)))
          .isEqualTo(NORMAL_PAID_POINT);
    }

    @DisplayName("주문일이 프로모션 시작일과 같으면 프로모션 적립금을 반환한다.")
    @Test
    void paydate_is_startdate() {
      RecommendationPointHistoryUseCase subject = subject();
      assertThat(subject.getPaidPoint(LocalDateTime.of(2021, 3, 1, 23, 0, 0)))
          .isEqualTo(PROMOTION_PAID_POINT);
    }

    @DisplayName("주문일이 프로모션 기간 안에 있으면 프로모션 적립금을 반환한다.")
    @Test
    void paydate_is_between() {
      RecommendationPointHistoryUseCase subject = subject();
      assertThat(subject.getPaidPoint(LocalDateTime.of(2021, 3, 2, 23, 0, 0)))
          .isEqualTo(PROMOTION_PAID_POINT);
    }

    @DisplayName("주문일이 프로모션 종료일과 같으면 프로모션 적립금을 반환한다.")
    @Test
    void paydate_is_enddate() {
      RecommendationPointHistoryUseCase subject = subject();
      assertThat(subject.getPaidPoint(LocalDateTime.of(2021, 3, 3, 23, 0, 0)))
          .isEqualTo(PROMOTION_PAID_POINT);
    }

    @DisplayName("주문일이 프로모션 종료일 이후이면 기본 적립금을 반환한다.")
    @Test
    void paydate_is_enddate_after() {
      RecommendationPointHistoryUseCase subject = subject();
      assertThat(subject.getPaidPoint(LocalDateTime.of(2021, 3, 4, 0, 0, 0)))
          .isEqualTo(NORMAL_PAID_POINT);
    }
  }
}
