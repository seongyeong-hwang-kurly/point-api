package com.kurly.cloud.point.api.point.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.exception.CancelAmountExceedException;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.service.ConsumePointUseCase;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("ConsumePointServiceTest class")
class ConsumePointServiceTest implements CommonTestGiven {
  @Autowired
  ConsumePointUseCase consumePointUseCase;

  @Autowired
  PublishPointUseCase publishPointUseCase;

  @Autowired
  MemberPointDomainService memberPointDomainService;

  void publishCashPoint(long amount) {
    publishPointUseCase.publish(PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .point(amount)
        .historyType(HistoryType.TYPE_1.getValue())
        .settle(true)
        .build());
  }

  void publishFreePoint(long amount) {
    publishPointUseCase.publish(PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .point(amount)
        .historyType(HistoryType.TYPE_1.getValue())
        .build());
  }

  @Nested
  @DisplayName("회원의 적립금을 사용 할 때")
  class DescribeConsume {

    PointConsumeResult subject(ConsumePointRequest request) throws NotEnoughPointException {
      return consumePointUseCase.consume(request);
    }

    @BeforeEach
    void publishPoint() {
      publishFreePoint(getFreeAmount());
      publishCashPoint(getCashAmount());
    }

    long getFreeAmount() {
      return 1000;
    }

    long getCashAmount() {
      return 2000;
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유한 보인트가 부족하면")
    class Context0 {

      @DisplayName("NotEnoughPointException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (NotEnoughPointException expected) {

        }
      }

      ConsumePointRequest givenRequest() {
        return ConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .point(10000L)
            .historyType(HistoryType.TYPE_100.getValue())
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유한 보인트가 충분 하면")
    class Context1 {

      @DisplayName("적립금을 사용처리 한다")
      @Test
      void test() throws NotEnoughPointException {
        PointConsumeResult subject = subject(givenRequest());

        assertThat(subject.getTotalConsumed()).isEqualTo(getFreeAmount() + getCashAmount());
        assertThat(subject.getTotalFreePointConsumed()).isEqualTo(getFreeAmount());
        assertThat(subject.getTotalCashPointConsumed()).isEqualTo(getCashAmount());
        assertThat(subject.getRemain()).isEqualTo(0);
      }

      ConsumePointRequest givenRequest() {
        return ConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .point(getFreeAmount() + getCashAmount())
            .detail("detail")
            .historyType(HistoryType.TYPE_100.getValue())
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유한 보인트중 일부만 사용하면")
    class Context2 {

      @DisplayName("무상적립금을 먼저 사용하고 유상적립금을 사용 한다")
      @Test
      void test() throws NotEnoughPointException {
        PointConsumeResult subject = subject(givenRequest());

        assertThat(subject.getTotalConsumed()).isEqualTo(getCashAmount());
        assertThat(subject.getTotalFreePointConsumed()).isEqualTo(getFreeAmount());
        assertThat(subject.getTotalCashPointConsumed())
            .isEqualTo(getCashAmount() - getFreeAmount());
        assertThat(subject.getRemain()).isEqualTo(0);

      }

      ConsumePointRequest givenRequest() {
        return ConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .point(getCashAmount())
            .detail("detail")
            .historyType(HistoryType.TYPE_100.getValue())
            .build();
      }
    }

    @Nested
    @DisplayName("유상적립금을 사용할 때")
    class Context3 {
      @TransactionalTest
      @Nested
      @DisplayName("전체 적립금이 충분하고 유상적립금이 부족하면")
      class Context0 {

        @DisplayName("NotEnoughPointException 예외가 발생 한다")
        @Test
        void test() {
          try {
            subject(givenRequest());
            fail("실행되면 안되는 코드");
          } catch (NotEnoughPointException expected) {

          }
        }

        ConsumePointRequest givenRequest() {
          return ConsumePointRequest.builder()
              .memberNumber(givenMemberNumber())
              .point(getCashAmount() + getFreeAmount())
              .detail("detail")
              .settle(true)
              .historyType(HistoryType.TYPE_100.getValue())
              .build();
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("유상적립금이 충분하면")
      class Context1 {

        @DisplayName("유상적립금을 사용 하고 무상적립금은 사용하지 않는다")
        @Test
        void test() throws NotEnoughPointException {
          PointConsumeResult subject = subject(givenRequest());

          assertThat(subject.getTotalConsumed()).isEqualTo(getCashAmount());
          assertThat(subject.getTotalFreePointConsumed()).isEqualTo(0);
          assertThat(subject.getTotalCashPointConsumed()).isEqualTo(getCashAmount());
          assertThat(subject.getRemain()).isEqualTo(0);
        }

        ConsumePointRequest givenRequest() {
          return ConsumePointRequest.builder()
              .memberNumber(givenMemberNumber())
              .point(getCashAmount())
              .detail("detail")
              .settle(true)
              .historyType(HistoryType.TYPE_100.getValue())
              .build();
        }
      }
    }
  }

  @Nested
  @DisplayName("회원이 주문으로 인해 적립금을 사용 할 때")
  class DescribeConsumeByOrder {

    PointConsumeResult subject(OrderConsumePointRequest request) throws NotEnoughPointException {
      return consumePointUseCase.consumeByOrder(request);
    }

    @BeforeEach
    void publishPoint() {
      publishFreePoint(getFreeAmount());
      publishCashPoint(getCashAmount());
    }

    long getFreeAmount() {
      return 1000;
    }

    long getCashAmount() {
      return 2000;
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유한 보인트가 부족하면")
    class Context0 {

      @DisplayName("NotEnoughPointException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (NotEnoughPointException expected) {

        }
      }

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(10000L)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유한 보인트가 충분 하면")
    class Context1 {

      @DisplayName("적립금을 사용처리 한다")
      @Test
      void test() throws NotEnoughPointException {
        PointConsumeResult subject = subject(givenRequest());

        assertThat(subject.getTotalConsumed()).isEqualTo(getFreeAmount() + getCashAmount());
        assertThat(subject.getTotalFreePointConsumed()).isEqualTo(getFreeAmount());
        assertThat(subject.getTotalCashPointConsumed()).isEqualTo(getCashAmount());
        assertThat(subject.getRemain()).isEqualTo(0);
      }

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(getFreeAmount() + getCashAmount())
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("보유한 보인트중 일부만 사용하면")
    class Context2 {

      @DisplayName("무상적립금을 먼저 사용하고 유상적립금을 사용 한다")
      @Test
      void test() throws NotEnoughPointException {
        PointConsumeResult subject = subject(givenRequest());

        assertThat(subject.getTotalConsumed()).isEqualTo(getCashAmount());
        assertThat(subject.getTotalFreePointConsumed()).isEqualTo(getFreeAmount());
        assertThat(subject.getTotalCashPointConsumed())
            .isEqualTo(getCashAmount() - getFreeAmount());
        assertThat(subject.getRemain()).isEqualTo(0);

      }

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(getCashAmount())
            .build();
      }
    }

  }

  @Nested
  @DisplayName("회원이 주문에 사용한 적립금을 사용 취소 할 때")
  class DescribeCancelConsumeByOrder {

    void givenConsumeByOrder() {
      try {
        publishFreePoint(givenFreeAmount());
        consumePointUseCase.consumeByOrder(OrderConsumePointRequest.builder()
            .memberNumber(givenMemberNumber())
            .orderNumber(givenOrderNumber())
            .point(givenFreeAmount())
            .build());
      } catch (NotEnoughPointException e) {

      }
    }

    long givenFreeAmount() {
      return 1000L;
    }

    void subject(long amount) throws CancelAmountExceedException {
      consumePointUseCase.cancelConsumeByOrder(CancelOrderConsumePointRequest.builder()
          .actionMemberNumber(givenMemberNumber())
          .memberNumber(givenMemberNumber())
          .orderNumber(givenOrderNumber())
          .point(amount)
          .build());
    }

    @TransactionalTest
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

    @TransactionalTest
    @Nested
    @DisplayName("사용 취소 하면")
    class Context1 {

      @Test
      @DisplayName("취소한 만큼 적립금이 적립 된다")
      void test() throws CancelAmountExceedException {
        givenConsumeByOrder();
        subject(givenFreeAmount());
        MemberPoint memberPoint =
            memberPointDomainService.getOrCreateMemberPoint(givenMemberNumber());

        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenFreeAmount());
        assertThat(memberPoint.getFreePoint()).isEqualTo(givenFreeAmount());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);

      }
    }
  }
}
