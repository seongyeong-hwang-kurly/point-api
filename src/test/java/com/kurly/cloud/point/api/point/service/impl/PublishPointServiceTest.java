package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.domain.publish.CancelPublishOrderPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.service.PointHistoryUseCase;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@DisplayName("PublishPointServiceTest class")
class PublishPointServiceTest implements CommonTestGiven {

  @Autowired
  PublishPointUseCase publishPointUseCase;
  @Autowired
  PointHistoryUseCase pointHistoryUseCase;
  @Autowired
  PointDomainService pointDomainService;
  @Autowired
  MemberPointDomainService memberPointDomainService;
  @Autowired
  MemberPointHistoryDomainService memberPointHistoryDomainService;

  @Nested
  @DisplayName("주문 적립 적립금을 발행 할 때")
  class DescribePublishByOrder {
    PublishPointRequest given(long memberNumber, long orderNumber) {
      return PublishPointRequest.builder()
          .orderNumber(orderNumber)
          .pointRatio(7)
          .point(1000L)
          .memberNumber(memberNumber)
          .build();
    }

    @TransactionalTest
    @Nested
    @DisplayName("올바른 값이 입력되면")
    class Context0 {
      public static final int MEMBER_NUMBER = 108;
      public static final int ORDER_NUMBER = 1245678910;
      @Test
      @DisplayName("주문번호로 무상 적립금이 발행된다")
      void test() throws AlreadyPublishedException {
        PublishPointRequest request = given(MEMBER_NUMBER, ORDER_NUMBER);
        publishPointUseCase.publishByOrder(request);
        MemberPoint memberPoint =
            memberPointDomainService.getOrCreateMemberPoint(MEMBER_NUMBER);

        assertThat(memberPoint.getTotalPoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getFreePoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
        assertThat(memberPoint.getUpdateTime()).isEqualToIgnoringSeconds(LocalDateTime.now());

        Page<MemberPointHistory> historyList = memberPointHistoryDomainService.getHistoryList(
            MemberPointHistoryListRequest.builder()
                .memberNumber(MEMBER_NUMBER)
                .build()
        );

        MemberPointHistory memberPointHistory = historyList.getContent().get(0);
        assertThat(memberPointHistory.getOrderNumber()).isEqualTo(request.getOrderNumber());
        assertThat(memberPointHistory.getHistoryType()).isEqualTo(HistoryType.TYPE_1.getValue());
        assertThat(memberPointHistory.getTotalPoint()).isEqualTo(request.getPoint());
        assertThat(memberPointHistory.getFreePoint()).isEqualTo(request.getPoint());
        assertThat(memberPointHistory.getCashPoint()).isEqualTo(0);
        assertThat(memberPointHistory.isHidden()).isEqualTo(false);
        assertThat(memberPointHistory.getExpireTime())
            .isEqualToIgnoringNanos(
                PointExpireDateCalculator.calculateDefault(LocalDateTime.now()));

        List<Point> availableMemberPoint =
            pointDomainService.getAvailableMemberPoint(MEMBER_NUMBER);
        assertThat(availableMemberPoint.size()).isEqualTo(1);

        Point point = availableMemberPoint.get(0);
        assertThat(point.getOrderNumber()).isEqualTo(request.getOrderNumber());
        assertThat(point.getCharge()).isEqualTo(request.getPoint());
        assertThat(point.getRemain()).isEqualTo(request.getPoint());
        assertThat(point.getPointRatio()).isEqualTo(request.getPointRatio());
        assertThat(point.getHistoryType()).isEqualTo(HistoryType.TYPE_1.getValue());
        assertThat(point.isPayment()).isEqualTo(false);
        assertThat(point.isSettle()).isEqualTo(false);
        assertThat(point.getExpireTime())
            .isEqualTo(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()));

        assertThat(pointDomainService.getPublishedByOrderNumber(request.getOrderNumber()))
            .isNotEmpty();

      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("중복발행을 시도 하면")
    class Context1 {
      public static final int MEMBER_NUMBER = 109;
      public static final int ORDER_NUMBER = 1245678911;

      @DisplayName("AlreadyPublishedException이 발생 한다")
      @Test
      void test() {
        PublishPointRequest request = given(MEMBER_NUMBER, ORDER_NUMBER);
        try {
          publishPointUseCase.publishByOrder(request);
          publishPointUseCase.publishByOrder(request);
          fail("실행되면 안되는 코드");
        } catch (AlreadyPublishedException e) {

        }
      }
    }
  }

  @Nested
  @DisplayName("관리자(시스템)가 적립금을 발행 할 때")
  class DescribePublish {
    PublishPointRequest given(long memberNumber) {
      return PublishPointRequest.builder()
          .point(1000L)
          .memberNumber(memberNumber)
          .historyType(HistoryType.TYPE_12.getValue())
          .actionMemberNumber(givenMemberNumber())
          .detail("추천 지급")
          .memo("내맘대로")
          .build();
    }

    @TransactionalTest
    @Nested
    @DisplayName("올바른 값이 입력되면")
    class Context0 {
      public static final int MEMBER_NUM = 107;
      @Test
      @DisplayName("적립금이 발행된다")
      void test() {
        PublishPointRequest request = given(MEMBER_NUM);
        publishPointUseCase.publish(request);
        MemberPoint memberPoint =
            memberPointDomainService.getOrCreateMemberPoint(MEMBER_NUM);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getFreePoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }

  }

  @Nested
  @DisplayName("적립금이 -1000 포인트인 상태에서,")
  class DescribeRepay {

    public static final int MINUS_POINT = 1000;

    MemberPoint whenMinusPointCommon() {
      return whenMinusPointBy(givenMemberNumber(), givenOrderNumber(), MINUS_POINT);
    }

    MemberPoint whenMinusPointBy(long memberNumber, long orderNumber, long extractMoney) {
      publishPointUseCase.cancelPublishByOrder(CancelPublishOrderPointRequest.builder()
              .actionMemberNumber(memberNumber)
              .memberNumber(memberNumber)
              .orderNumber(orderNumber)
              .point(extractMoney)
              .build());
      return memberPointDomainService.getOrCreateMemberPoint(memberNumber);
    }

    @Nested
    @DisplayName("무상적립금을,")
    class ContextOnPublishFree {

      public static final int ORDER_NUMBER = 112233;

      void giveFreePoint(long toWhom, long amount) {
        publishPointUseCase.publish(PublishPointRequest.builder()
            .point(amount)
            .memberNumber(toWhom)
            .historyType(HistoryType.TYPE_12.getValue())
            .build());
      }

      @TransactionalTest
      @Nested
      @DisplayName("2000포인트 지급할 경우,")
      class Context0 {

        public static final int MORE_DEBT_MEMBER = 100;
        public static final int MORE_POINT = 2000;

        @Test
        @DisplayName("여전히 잔액은 지급한 만큼만 줄어든 마이너스가 되어야 한다.")
        void test() {
          MemberPoint before= whenMinusPointBy(MORE_DEBT_MEMBER, ORDER_NUMBER, MINUS_POINT);
          assertThat(before.getTotalPoint()).isEqualTo(-MINUS_POINT);
          giveFreePoint(MORE_DEBT_MEMBER, MORE_POINT);
          MemberPoint after = memberPointDomainService.getMemberPoint(MORE_DEBT_MEMBER).get();
          assertThat(after.getTotalPoint()).isEqualTo(2000 - MINUS_POINT);
          assertThat(after.getFreePoint()).isEqualTo(2000 - MINUS_POINT);
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("더 적게 지급할 경우,")
      class Context1 {

        public static final int MORE_POINT_MEMBER = 101;

        @Test
        @DisplayName("마이너스 금액만큼 뺀 금액이 잔액이 되어야 한다.")
        void test() {
          MemberPoint before= whenMinusPointBy(MORE_POINT_MEMBER, ORDER_NUMBER, MINUS_POINT);
          assertThat(before.getTotalPoint()).isEqualTo(-MINUS_POINT);
          giveFreePoint(MORE_POINT_MEMBER,  givenAmount());
          MemberPoint after = memberPointDomainService.getMemberPoint(MORE_POINT_MEMBER).get();
          assertThat(after.getTotalPoint()).isEqualTo(givenAmount() - MINUS_POINT);
          assertThat(after.getFreePoint()).isEqualTo(givenAmount() - MINUS_POINT);
        }

        int givenAmount() {
          return 500;
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("대출적립금과 지급적립금이 같으면")
      class Context2 {

        public static final int SAME_POINT_MEMBER = 102;
        public static final int SAME_POINT = MINUS_POINT;

        @Test
        @DisplayName("적립금을 지급하고 전액 차감한다")
        void test() {
          MemberPoint before = whenMinusPointBy(SAME_POINT_MEMBER, ORDER_NUMBER, MINUS_POINT);
          assertThat(before.getTotalPoint()).isEqualTo(-MINUS_POINT);
          giveFreePoint(SAME_POINT_MEMBER, SAME_POINT);
          MemberPoint after = memberPointDomainService.getMemberPoint(SAME_POINT_MEMBER).get();
          assertThat(after.getTotalPoint()).isEqualTo(0);
          assertThat(after.getFreePoint()).isEqualTo(0);
        }

      }
    }

    @Nested
    @DisplayName("유상적립금을 지급하면")
    class ContextOnCashPoint {

      void givePaidPoint(long memberNumber, long amount) {
        publishPointUseCase.publish(PublishPointRequest.builder()
            .point(amount)
            .settle(true)
            .memberNumber(memberNumber)
            .historyType(HistoryType.TYPE_12.getValue())
            .build());
      }

      @TransactionalTest
      @Nested
      @DisplayName("대출적립금보다 지급적립금이 많으면")
      class Context0 {
        @Test
        @DisplayName("적립금을 지급하고 대출적립금만큼 차감한다")
        void test() {
          MemberPoint given = whenMinusPointBy(106, 12456789, MINUS_POINT);
          givePaidPoint(106, givenAmount());
          MemberPoint after = memberPointDomainService.getMemberPoint(106).get();
          assertThat(after.getTotalPoint()).isEqualTo(givenAmount() - MINUS_POINT);
          assertThat(after.getFreePoint()).isEqualTo(0);
          assertThat(after.getCashPoint()).isEqualTo(givenAmount() - MINUS_POINT);
        }

        int givenAmount() {
          return 2000;
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("지급적립금보다 대출적립금이 많으면")
      class Context1 {
        @Test
        @DisplayName("적립금을 지급하고 전액 차감한다")
        void test() {
          MemberPoint given = whenMinusPointBy(105, 12456789, 1000);
          givePaidPoint(105, givenAmount());
          MemberPoint after = memberPointDomainService.getMemberPoint(105).get();
          assertThat(after.getTotalPoint()).isEqualTo(givenAmount() - MINUS_POINT);
          assertThat(after.getFreePoint()).isEqualTo(givenAmount() - MINUS_POINT);
          assertThat(after.getCashPoint()).isEqualTo(0);
        }

        int givenAmount() {
          return 500;
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("대출적립금과 지급적립금이 같으면")
      class Context2 {
        public static final int PAID_SAME_POINT_MEMBER = 104;
        @Test
        @DisplayName("적립금을 지급하고 전액 차감한다")
        void test() {
          MemberPoint before = whenMinusPointBy(PAID_SAME_POINT_MEMBER, givenOrderNumber(), MINUS_POINT);
          assertThat(before.getTotalPoint()).isEqualTo(-MINUS_POINT);
          givePaidPoint(PAID_SAME_POINT_MEMBER, MINUS_POINT);
          MemberPoint after = memberPointDomainService.getMemberPoint(PAID_SAME_POINT_MEMBER).get();
          assertThat(after.getTotalPoint()).isEqualTo(0);
          assertThat(after.getFreePoint()).isEqualTo(0);
          assertThat(after.getCashPoint()).isEqualTo(0);
        }
      }
    }

  }

  @Nested
  @DisplayName("주문 적립 적립금을 발행을 취소 할 때")
  class DescribeCancelPublishByOrder {
    void givenOrderPoint(long memberNumber, long orderNumber) throws AlreadyPublishedException {
      publishPointUseCase.publishByOrder(PublishPointRequest.builder()
          .point(givenOrderPointAmount())
          .orderNumber(orderNumber)
          .memberNumber(memberNumber)
          .pointRatio(0.7f)
          .build()
      );
    }

    long givenOrderPointAmount() {
      return 1000;
    }

    void givenNonOrderPoint(long memberNubmer) {
      publishPointUseCase.publish(PublishPointRequest.builder()
          .point(givenNonOrderPointAmount())
          .memberNumber(memberNubmer)
          .historyType(HistoryType.TYPE_12.getValue())
          .build());
    }

    long givenNonOrderPointAmount() {
      return 2000;
    }

    MemberPoint cancelPublishByOrder(long memberNumber, long orderNumber, long amount) {
      publishPointUseCase.cancelPublishByOrder(CancelPublishOrderPointRequest.builder()
          .actionMemberNumber(memberNumber)
          .memberNumber(memberNumber)
          .orderNumber(orderNumber)
          .point(amount)
          .build());
      return memberPointDomainService.getOrCreateMemberPoint(memberNumber);
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유햔 적립금이 충분하면")
    class Context0 {
      @Test
      @DisplayName("적립된 적립금을 전부 회수(사용) 한다")
      void test() throws AlreadyPublishedException {
        givenNonOrderPoint(givenMemberNumber());
        givenOrderPoint(givenMemberNumber(), givenOrderNumber());
        MemberPoint subject = cancelPublishByOrder(givenMemberNumber(), givenOrderNumber(), cancelAmount());

        assertThat(subject.getTotalPoint())
            .isEqualTo(givenOrderPointAmount() + givenNonOrderPointAmount() - cancelAmount());
      }

      long cancelAmount() {
        return givenOrderPointAmount();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유햔 적립금이 충분하지 않으면")
    class Context1 {

      @Test
      @DisplayName("모자른 만큼 보유적립금이 대출(마이너스) 처리 된다")
      void test() throws AlreadyPublishedException {
        givenNonOrderPoint(givenMemberNumber()-1);
        givenOrderPoint(givenMemberNumber()-1, givenOrderNumber()-1);
        MemberPoint subject = cancelPublishByOrder(givenMemberNumber()-1, givenOrderNumber()-1, cancelAmount());

        assertThat(subject.getTotalPoint())
            .isEqualTo(givenOrderPointAmount() + givenNonOrderPointAmount() - cancelAmount());
      }

      int cancelAmount() {
        return 10000;
      }
    }
  }
}
