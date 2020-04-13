package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.HistoryType;
import com.kurly.cloud.point.api.point.domain.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.exception.CancelAmountExceedException;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.service.port.in.ConsumePointPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("ConsumePointService class")
class ConsumePointServiceTest {
  @Autowired
  ConsumePointPort consumePointPort;

  @Autowired
  PublishPointService publishPointService;

  @Autowired
  MemberPointService memberPointService;

  long givenMemberNumber() {
    return 999999999;
  }

  long givenOrderNumber() {
    return 888888888;
  }

  void publishCashPoint(int amount) {
    publishPointService.publish(PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .point(amount)
        .historyType(HistoryType.TYPE_1.getValue())
        .settle(true)
        .build());
  }

  void publishFreePoint(int amount) {
    publishPointService.publish(PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .point(amount)
        .historyType(HistoryType.TYPE_1.getValue())
        .build());
  }

  @Nested
  @DisplayName("회원의 적립금을 사용 할 때")
  class DescribeConsume {

    PointConsumeResult subject(ConsumePointRequest request) throws NotEnoughPointException {
      return consumePointPort.consume(request);
    }

    int getFreeAmount() {
      return 1000;
    }

    int getCashAmount() {
      return 2000;
    }

    @BeforeEach
    void publishPoint() {
      publishFreePoint(getFreeAmount());
      publishCashPoint(getCashAmount());
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("보유한 보인트가 부족하면")
    class Context0 {

      ConsumePointRequest givenRequest() {
        return ConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .point(10000)
            .historyType(HistoryType.TYPE_100.getValue())
            .build();
      }

      @DisplayName("NotEnoughPointException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (NotEnoughPointException expected) {

        }
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("보유한 보인트가 충분 하면")
    class Context1 {

      ConsumePointRequest givenRequest() {
        return ConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .point(getFreeAmount() + getCashAmount())
            .detail("detail")
            .historyType(HistoryType.TYPE_100.getValue())
            .build();
      }

      @DisplayName("포인트를 사용처리 한다")
      @Test
      void test() throws NotEnoughPointException {
        PointConsumeResult subject = subject(givenRequest());

        assertThat(subject.getTotalConsumed()).isEqualTo(getFreeAmount() + getCashAmount());
        assertThat(subject.getTotalFreePointConsumed()).isEqualTo(getFreeAmount());
        assertThat(subject.getTotalCashPointConsumed()).isEqualTo(getCashAmount());
        assertThat(subject.getRemain()).isEqualTo(0);
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("보유한 보인트중 일부만 사용하면")
    class Context2 {

      ConsumePointRequest givenRequest() {
        return ConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .point(getCashAmount())
            .detail("detail")
            .historyType(HistoryType.TYPE_100.getValue())
            .build();
      }

      @DisplayName("무상포인트를 먼저 사용하고 유상포인트를 사용 한다")
      @Test
      void test() throws NotEnoughPointException {
        PointConsumeResult subject = subject(givenRequest());

        assertThat(subject.getTotalConsumed()).isEqualTo(getCashAmount());
        assertThat(subject.getTotalFreePointConsumed()).isEqualTo(getFreeAmount());
        assertThat(subject.getTotalCashPointConsumed()).isEqualTo(getCashAmount() - getFreeAmount());
        assertThat(subject.getRemain()).isEqualTo(0);

      }
    }

    @Nested
    @DisplayName("유상포인트를 사용할 때")
    class Context3 {
      @SpringBootTest
      @Transactional
      @Nested
      @DisplayName("전체 포인트가 충분하고 유상포인트가 부족하면")
      class Context0 {

        ConsumePointRequest givenRequest() {
          return ConsumePointRequest.builder()
              .memberNumber(givenMemberNumber())
              .point(getCashAmount() + getFreeAmount())
              .detail("detail")
              .settle(true)
              .historyType(HistoryType.TYPE_100.getValue())
              .build();
        }

        @DisplayName("NotEnoughPointException 예외가 발생 한다")
        @Test
        void test() {
          try {
            subject(givenRequest());
            fail("실행되면 안되는 코드");
          } catch (NotEnoughPointException expected) {

          }
        }
      }

      @SpringBootTest
      @Transactional
      @Nested
      @DisplayName("유상포인트가 충분하면")
      class Context1 {

        ConsumePointRequest givenRequest() {
          return ConsumePointRequest.builder()
              .memberNumber(givenMemberNumber())
              .point(getCashAmount())
              .detail("detail")
              .settle(true)
              .historyType(HistoryType.TYPE_100.getValue())
              .build();
        }

        @DisplayName("유상포인트를 사용 하고 무상포인트는 사용하지 않는다")
        @Test
        void test() throws NotEnoughPointException {
          PointConsumeResult subject = subject(givenRequest());

          assertThat(subject.getTotalConsumed()).isEqualTo(getCashAmount());
          assertThat(subject.getTotalFreePointConsumed()).isEqualTo(0);
          assertThat(subject.getTotalCashPointConsumed()).isEqualTo(getCashAmount());
          assertThat(subject.getRemain()).isEqualTo(0);
        }
      }
    }
  }

  @Nested
  @DisplayName("회원이 주문으로 인해 적립금을 사용 할 때")
  class DescribeConsumeByOrder {

    PointConsumeResult subject(OrderConsumePointRequest request) throws NotEnoughPointException {
      return consumePointPort.consumeByOrder(request);
    }

    int getFreeAmount() {
      return 1000;
    }

    int getCashAmount() {
      return 2000;
    }

    @BeforeEach
    void publishPoint() {
      publishFreePoint(getFreeAmount());
      publishCashPoint(getCashAmount());
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("보유한 보인트가 부족하면")
    class Context0 {

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(10000)
            .build();
      }

      @DisplayName("NotEnoughPointException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (NotEnoughPointException expected) {

        }
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("보유한 보인트가 충분 하면")
    class Context1 {

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(getFreeAmount() + getCashAmount())
            .build();
      }

      @DisplayName("포인트를 사용처리 한다")
      @Test
      void test() throws NotEnoughPointException {
        PointConsumeResult subject = subject(givenRequest());

        assertThat(subject.getTotalConsumed()).isEqualTo(getFreeAmount() + getCashAmount());
        assertThat(subject.getTotalFreePointConsumed()).isEqualTo(getFreeAmount());
        assertThat(subject.getTotalCashPointConsumed()).isEqualTo(getCashAmount());
        assertThat(subject.getRemain()).isEqualTo(0);
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("보유한 보인트중 일부만 사용하면")
    class Context2 {

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(getCashAmount())
            .build();
      }

      @DisplayName("무상포인트를 먼저 사용하고 유상포인트를 사용 한다")
      @Test
      void test() throws NotEnoughPointException {
        PointConsumeResult subject = subject(givenRequest());

        assertThat(subject.getTotalConsumed()).isEqualTo(getCashAmount());
        assertThat(subject.getTotalFreePointConsumed()).isEqualTo(getFreeAmount());
        assertThat(subject.getTotalCashPointConsumed()).isEqualTo(getCashAmount() - getFreeAmount());
        assertThat(subject.getRemain()).isEqualTo(0);

      }
    }

  }

  @Nested
  @DisplayName("회원이 주문에 사용한 적립금을 사용 취소 할 때")
  class DescribeCancelConsumeByOrder {

    int givenFreeAmount() {
      return 1000;
    }

    void givenConsumeByOrder() {
      try {
        publishFreePoint(givenFreeAmount());
        consumePointPort.consumeByOrder(OrderConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .orderNumber(givenOrderNumber())
            .point(givenFreeAmount())
            .build());
      } catch (NotEnoughPointException e) {

      }
    }

    void subject(int amount) throws CancelAmountExceedException {
      consumePointPort.cancelConsumeByOrder(CancelOrderConsumePointRequest.builder()
          .actionMemberNumber(givenMemberNumber())
          .memberNumber(givenMemberNumber())
          .orderNumber(givenOrderNumber())
          .point(amount)
          .build());
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("주문에 사용한 적립금보다 많은 적립금을 사용 취소 하면")
    class Context0 {

      @Test
      @DisplayName("CancelAmountExceedException이 발생 한다")
      void test() {
        givenConsumeByOrder();
        try {
          subject(10000);
          fail("실행되면 안되는 코드");
        } catch (CancelAmountExceedException e) {

        }
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("사용 취소 하면")
    class Context1 {

      @Test
      @DisplayName("취소한 만큼 적립금이 적립 된다")
      void test() throws CancelAmountExceedException {
        givenConsumeByOrder();
        subject(givenFreeAmount());
        MemberPoint memberPoint = memberPointService.getOrCrateMemberPoint(givenMemberNumber());

        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenFreeAmount());
        assertThat(memberPoint.getFreePoint()).isEqualTo(givenFreeAmount());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);

      }
    }
  }
}
