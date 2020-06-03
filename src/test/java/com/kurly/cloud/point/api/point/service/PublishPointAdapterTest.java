package com.kurly.cloud.point.api.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


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
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("PublishPointAdapter class")
class PublishPointAdapterTest implements CommonTestGiven {
  @Autowired
  PublishPointPort publishPointPort;
  @Autowired
  PointService pointService;
  @Autowired
  PointHistoryService pointHistoryService;
  @Autowired
  MemberPointService memberPointService;
  @Autowired
  MemberPointHistoryService memberPointHistoryService;

  @Nested
  @DisplayName("주문 적립 적립금을 발행 할 때")
  class DescribePublishByOrder {
    PublishPointRequest given() {
      return PublishPointRequest.builder()
          .orderNumber(8888888888888L)
          .pointRatio(7)
          .point(1000)
          .memberNumber(givenMemberNumber())
          .build();
    }

    @TransactionalTest
    @Nested
    @DisplayName("올바른 값이 입력되면")
    class Context0 {
      @Test
      @DisplayName("주문번호로 무상 적립금이 발행된다")
      void test() throws AlreadyPublishedException {
        PublishPointRequest request = given();
        publishPointPort.publishByOrder(request);
        MemberPoint memberPoint = memberPointService.getOrCreateMemberPoint(givenMemberNumber());

        assertThat(memberPoint.getTotalPoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getFreePoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
        assertThat(memberPoint.getUpdateTime()).isEqualToIgnoringSeconds(LocalDateTime.now());

        Page<MemberPointHistory> historyList = memberPointHistoryService.getHistoryList(
            MemberPointHistoryListRequest.builder()
                .memberNumber(givenMemberNumber())
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
            .isEqualTo(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()));

        List<Point> availableMemberPoint =
            pointService.getAvailableMemberPoint(givenMemberNumber());
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

        assertThat(pointService.getPublishedByOrderNumber(request.getOrderNumber())).isNotEmpty();

      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("중복발행을 시도 하면")
    class Context1 {
      @DisplayName("AlreadyPublishedException이 발생 한다")
      @Test
      void test() {
        PublishPointRequest request = given();
        try {
          publishPointPort.publishByOrder(request);
          publishPointPort.publishByOrder(request);
          fail("실행되면 안되는 코드");
        } catch (AlreadyPublishedException e) {

        }
      }
    }
  }

  @Nested
  @DisplayName("관리자(시스템)가 적립금을 발행 할 때")
  class DescribePublish {
    PublishPointRequest given() {
      return PublishPointRequest.builder()
          .point(1000)
          .memberNumber(givenMemberNumber())
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
      @Test
      @DisplayName("적립금이 발행된다")
      void test() {
        PublishPointRequest request = given();
        publishPointPort.publish(request);
        MemberPoint memberPoint = memberPointService.getOrCreateMemberPoint(givenMemberNumber());
        assertThat(memberPoint.getTotalPoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getFreePoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }

  }

  @Nested
  @DisplayName("대출한 적립금이 있을 때")
  class DescribeRepay {

    MemberPoint given() {
      publishPointPort.cancelPublishByOrder(CancelPublishOrderPointRequest.builder()
          .actionMemberNumber(givenMemberNumber())
          .memberNumber(givenMemberNumber())
          .orderNumber(givenOrderNumber())
          .point(givenDebtAmount())
          .build());
      return memberPointService.getOrCreateMemberPoint(givenMemberNumber());
    }

    int givenDebtAmount() {
      return 1000;
    }

    @Nested
    @DisplayName("무상적립금을 지급하면")
    class ContextOnPublishFree {

      void subject(int amount) {
        publishPointPort.publish(PublishPointRequest.builder()
            .point(amount)
            .memberNumber(givenMemberNumber())
            .historyType(HistoryType.TYPE_12.getValue())
            .build());
      }

      @TransactionalTest
      @Nested
      @DisplayName("대출적립금 보다 지급적립금이 많으면")
      class Context0 {
        @Test
        @DisplayName("적립금을 지급하고 대출적립금만큼 차감한다")
        void test() {
          MemberPoint given = given();
          subject(givenAmount());
          assertThat(given.getTotalPoint()).isEqualTo(givenAmount() - givenDebtAmount());
          assertThat(given.getFreePoint()).isEqualTo(givenAmount() - givenDebtAmount());
        }

        int givenAmount() {
          return 2000;
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("지급적립금 보다 대출적립금이 많으면")
      class Context1 {
        @Test
        @DisplayName("적립금을 지급하고 전액 차감한다")
        void test() {
          MemberPoint given = given();
          subject(givenAmount());
          assertThat(given.getTotalPoint()).isEqualTo(givenAmount() - givenDebtAmount());
          assertThat(given.getFreePoint()).isEqualTo(givenAmount() - givenDebtAmount());
        }

        int givenAmount() {
          return 500;
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("대출적립금과 지급적립금이 같으면")
      class Context2 {
        @Test
        @DisplayName("적립금을 지급하고 전액 차감한다")
        void test() {
          MemberPoint given = given();
          subject(givenAmount());
          assertThat(given.getTotalPoint()).isEqualTo(0);
          assertThat(given.getFreePoint()).isEqualTo(0);
        }

        int givenAmount() {
          return 1000;
        }
      }
    }

    @Nested
    @DisplayName("유상적립금을 지급하면")
    class ContextOnCashPoint {

      void subject(int amount) {
        publishPointPort.publish(PublishPointRequest.builder()
            .point(amount)
            .settle(true)
            .memberNumber(givenMemberNumber())
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
          MemberPoint given = given();
          subject(givenAmount());
          assertThat(given.getTotalPoint()).isEqualTo(givenAmount() - givenDebtAmount());
          assertThat(given.getFreePoint()).isEqualTo(0);
          assertThat(given.getCashPoint()).isEqualTo(givenAmount() - givenDebtAmount());
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
          MemberPoint given = given();
          subject(givenAmount());
          assertThat(given.getTotalPoint()).isEqualTo(givenAmount() - givenDebtAmount());
          assertThat(given.getFreePoint()).isEqualTo(givenAmount() - givenDebtAmount());
          assertThat(given.getCashPoint()).isEqualTo(0);
        }

        int givenAmount() {
          return 500;
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("대출적립금과 지급적립금이 같으면")
      class Context2 {
        @Test
        @DisplayName("적립금을 지급하고 전액 차감한다")
        void test() {
          MemberPoint given = given();
          subject(givenAmount());
          assertThat(given.getTotalPoint()).isEqualTo(0);
          assertThat(given.getFreePoint()).isEqualTo(0);
          assertThat(given.getCashPoint()).isEqualTo(0);
        }

        int givenAmount() {
          return 1000;
        }
      }
    }

  }

  @Nested
  @DisplayName("주문 적립 적립금을 발행을 취소 할 때")
  class DescribeCancelPublishByOrder {
    void givenOrderPoint() throws AlreadyPublishedException {
      publishPointPort.publishByOrder(PublishPointRequest.builder()
          .point(givenOrderPointAmount())
          .orderNumber(givenOrderNumber())
          .memberNumber(givenMemberNumber())
          .pointRatio(0.7f)
          .build()
      );
    }

    int givenOrderPointAmount() {
      return 1000;
    }

    void givenNonOrderPoint() {
      publishPointPort.publish(PublishPointRequest.builder()
          .point(givenNonOrderPointAmount())
          .memberNumber(givenMemberNumber())
          .historyType(HistoryType.TYPE_12.getValue())
          .build());
    }

    int givenNonOrderPointAmount() {
      return 2000;
    }

    MemberPoint subject(int amount) {
      publishPointPort.cancelPublishByOrder(CancelPublishOrderPointRequest.builder()
          .actionMemberNumber(givenMemberNumber())
          .memberNumber(givenMemberNumber())
          .orderNumber(givenOrderNumber())
          .point(amount)
          .build());
      return memberPointService.getOrCreateMemberPoint(givenMemberNumber());
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유햔 적립금이 충분하면")
    class Context0 {
      @Test
      @DisplayName("적립된 적립금을 전부 회수(사용) 한다")
      void test() throws AlreadyPublishedException {
        givenNonOrderPoint();
        givenOrderPoint();
        MemberPoint subject = subject(givenAmount());

        assertThat(subject.getTotalPoint())
            .isEqualTo(givenOrderPointAmount() + givenNonOrderPointAmount() - givenAmount());
      }

      int givenAmount() {
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
        givenNonOrderPoint();
        givenOrderPoint();
        MemberPoint subject = subject(givenAmount());

        assertThat(subject.getTotalPoint())
            .isEqualTo(givenOrderPointAmount() + givenNonOrderPointAmount() - givenAmount());
      }

      int givenAmount() {
        return 10000;
      }
    }
  }
}
