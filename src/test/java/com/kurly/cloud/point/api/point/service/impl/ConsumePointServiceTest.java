package com.kurly.cloud.point.api.point.service.impl;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@DisplayName("ConsumePointServiceTest class")
class ConsumePointServiceTest {
  public static final Random RANDOM = new Random();
  @Autowired
  ConsumePointUseCase consumePointUseCase;

  @Autowired
  PublishPointUseCase publishPointUseCase;

  @Autowired
  MemberPointDomainService memberPointDomainService;

  private long memberNumber = 0;
  private long orderNumber = 0;

  long givenMemberNumber() {
    return memberNumber;
  }
  long givenOrderNumber() { return orderNumber; }

  @BeforeEach
  void initMemberNOrderNumber() {
    memberNumber = RANDOM.nextLong();
    orderNumber = RANDOM.nextLong();
  }

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

  void publishExpiredFreePoint(long amount) {
    publishPointUseCase.publish(PublishPointRequest.builder()
        .memberNumber(givenMemberNumber())
        .point(amount)
        .historyType(HistoryType.TYPE_1.getValue())
        .expireDate(LocalDateTime.now().minusDays(1))
        .build());
  }

  @Nested
  @DisplayName("????????? ???????????? ?????? ??? ???")
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
    @DisplayName("????????? ???????????? ????????????")
    class Context0 {

      @DisplayName("NotEnoughPointException ????????? ?????? ??????")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("???????????? ????????? ??????");
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
    @DisplayName("????????? ???????????? ?????? ??????")
    class Context1 {

      @DisplayName("???????????? ???????????? ??????")
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
    @DisplayName("????????? ???????????? ????????? ????????????")
    class Context2 {

      @DisplayName("?????????????????? ?????? ???????????? ?????????????????? ?????? ??????")
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
    @DisplayName("?????????????????? ????????? ???")
    class Context3 {
      @TransactionalTest
      @Nested
      @DisplayName("?????? ???????????? ???????????? ?????????????????? ????????????")
      class Context0 {

        @DisplayName("NotEnoughPointException ????????? ?????? ??????")
        @Test
        void test() {
          try {
            subject(givenRequest());
            fail("???????????? ????????? ??????");
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
      @DisplayName("?????????????????? ????????????")
      class Context1 {

        @DisplayName("?????????????????? ?????? ?????? ?????????????????? ???????????? ?????????")
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
  @DisplayName("????????? ???????????? ?????? ???????????? ?????? ??? ???")
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
    @DisplayName("????????? ???????????? ????????????")
    class Context0 {

      @DisplayName("NotEnoughPointException ????????? ?????? ??????")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("???????????? ????????? ??????");
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
    @DisplayName("????????? ???????????? ?????? ??????")
    class Context1 {

      @DisplayName("???????????? ???????????? ??????")
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
    @DisplayName("????????? ???????????? ????????? ????????????")
    class Context2 {

      @DisplayName("?????????????????? ?????? ???????????? ?????????????????? ?????? ??????")
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
  @DisplayName("???????????? ?????? ???????????? ?????? ???")
  class DescribeExpiredPointConsumed {

    @BeforeEach
    void publishPoint() {
      publishFreePoint(getFreeAmount());
      publishExpiredFreePoint(getFreeAmount());
    }

    private long getFreeAmount() {
      return 1000;
    }

    PointConsumeResult subject(OrderConsumePointRequest request) throws NotEnoughPointException {
      return consumePointUseCase.consumeByOrder(request);
    }

    @TransactionalTest
    @Nested
    @DisplayName("???????????? ????????? ?????? ????????? ?????? ?????? ??????")
    class Context0 {

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(getFreeAmount())
            .build();
      }

      @Test
      @DisplayName("??????????????? ????????????")
      void test() throws NotEnoughPointException {
        PointConsumeResult pointConsumeResult = subject(givenRequest());

        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(getFreeAmount());
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("???????????? ?????? ???????????? ??????????????? ??????")
    class Context1 {

      OrderConsumePointRequest givenRequest() {
        return OrderConsumePointRequest.builder()
            .orderNumber(givenOrderNumber())
            .memberNumber(givenMemberNumber())
            .point(getFreeAmount() + getFreeAmount())
            .build();
      }

      @DisplayName("NotEnoughPointException ????????? ?????? ??????")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("???????????? ????????? ??????");
        } catch (NotEnoughPointException expected) {

        }
      }
    }
  }

  @Nested
  @DisplayName("????????? ????????? ????????? ???????????? ?????? ?????? ??? ???")
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
    @DisplayName("????????? ????????? ??????????????? ?????? ???????????? ?????? ?????? ??????")
    class Context0 {

      @Test
      @DisplayName("CancelAmountExceedException??? ?????? ??????")
      void test() {
        givenConsumeByOrder();
        try {
          subject(10000);
          fail("???????????? ????????? ??????");
        } catch (CancelAmountExceedException e) {

        }
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("?????? ?????? ??????")
    class Context1 {

      @Test
      @DisplayName("????????? ?????? ???????????? ?????? ??????")
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
