package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.repository.PointRepository;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@DisplayName("PointDomainServiceTest class")
class PointDomainServiceTest implements CommonTestGiven {

  @Autowired
  PointDomainService pointDomainService;

  @Autowired
  PointRepository pointRepository;

  @Nested
  @DisplayName("주문 적립금을 조회 할 때")
  class DescribegetPublishedByOrderNumber {
    Optional<Point> subject(long orderNumber) {
      return pointDomainService.getPublishedByOrderNumber(orderNumber);
    }

    void givenOrderPoint(long memberNumber, long orderNumber) {
      pointDomainService.publishPoint(PublishPointRequest.builder()
          .memberNumber(memberNumber)
          .orderNumber(orderNumber)
          .point(1000L)
          .historyType(HistoryType.TYPE_1.getValue())
          .build());
    }

    @Nested
    @DisplayName("주문 적립금이 없으면")
    class Context0 {
      private static final long ORDER_NUMBER = 1022000;

      @Test
      @DisplayName("비어 있는 Optional을 리턴한다")
      void test() {
        Optional<Point> subject = subject(ORDER_NUMBER);
        assertThat(subject).isEmpty();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("주문 적립금이 있으면")
    class Context1 {
      private static final long MEMBER_NUMBER = 1023;
      private static final long ORDER_NUMBER = 1023000;

      @Test
      @DisplayName("적립된 적립금을 포함하는 Optional을 리턴한다")
      void test() {
        givenOrderPoint(MEMBER_NUMBER, ORDER_NUMBER);
        Optional<Point> subject = subject(ORDER_NUMBER);
        assertThat(subject).isNotEmpty();
        assertThat(subject.get().getOrderNumber()).isEqualTo(ORDER_NUMBER);
        assertThat(subject.get().getMemberNumber()).isEqualTo(MEMBER_NUMBER);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("두개 이상의 동일 주문 적립금이 있으면")
    class Context2 {
      private static final long MEMBER_NUMBER = 1024;
      private static final long ORDER_NUMBER = 1024000;

      @Test
      @DisplayName("두개중 한개의 주문 적립금을 리턴한다")
      void test() {
        givenOrderPoint(MEMBER_NUMBER, ORDER_NUMBER);
        givenOrderPoint(MEMBER_NUMBER, ORDER_NUMBER);
        Optional<Point> subject = subject(ORDER_NUMBER);
        assertThat(subject).isNotEmpty();
        assertThat(subject.get().getOrderNumber()).isEqualTo(ORDER_NUMBER);
        assertThat(subject.get().getMemberNumber()).isEqualTo(MEMBER_NUMBER);
      }
    }
  }

  @Nested
  @DisplayName("적립금을 회원에게 지급 할 때")
  class DescribePublishPoint {
    Point subject(PublishPointRequest request) {
      return pointDomainService.publishPoint(request);
    }

    @Nested
    @DisplayName("memberNumber가 없으면")
    class Context0 {

      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }

      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .point(100L)
            .build();
      }
    }

    @Nested
    @DisplayName("point가 없으면")
    class Context1 {
      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }

      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .memberNumber(givenMemberNumber())
            .build();
      }
    }

    @Nested
    @DisplayName("historyType이 없으면")
    class Context2 {
      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }

      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .memberNumber(givenMemberNumber())
            .point(100L)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("모든값이 존재한다면")
    class Context3 {
      @DisplayName("값을 입력하고 입력된 값을 반환한다")
      @Test
      void test() {
        PublishPointRequest given = givenRequest();
        Point subject = subject(given);

        assertThat(subject.getSeq()).isNotZero();
        assertThat(subject.getMemberNumber()).isEqualTo(given.getMemberNumber());
        assertThat(subject.getOrderNumber()).isEqualTo(given.getOrderNumber());
        assertThat(subject.getCharge()).isEqualTo(given.getPoint());
        assertThat(subject.getRemain()).isEqualTo(given.getPoint());
        assertThat(subject.getPointRatio()).isEqualTo(given.getPointRatio());
        assertThat(subject.getHistoryType()).isEqualTo(given.getHistoryType());
        assertThat(subject.getRefundType()).isEqualTo(0);
        assertThat(subject.isPayment()).isEqualTo(given.isPayment());
        assertThat(subject.isSettle()).isEqualTo(given.isSettle());
        assertThat(subject.getRegTime()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(subject.getExpireTime())
            .isEqualTo(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()));
      }

      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .memberNumber(givenMemberNumber())
            .point(100L)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료일을 지정하면")
    class Context4 {
      @DisplayName("지정한 만료일의 시각이 23:59:59 으로 입력 한다")
      @Test
      void test() {
        PublishPointRequest given = givenRequest();
        Point subject = subject(given);

        assertThat(subject.getExpireTime())
            .isEqualTo(
                DateTimeUtil.toLocalDateTime(given.getExpireDate()).withHour(23).withMinute(59)
                    .withSecond(59));
      }

      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .memberNumber(givenMemberNumber())
            .point(100L)
            .expireDate(LocalDateTime.now().plusDays(1))
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료일의 제한이 없다면")
    class Context5 {
      @DisplayName("만료일은 NULL을 리턴한다")
      @Test
      void test() {
        PublishPointRequest given = givenRequest();
        Point subject = subject(given);

        assertThat(subject.getExpireTime()).isNull();
      }

      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .memberNumber(givenMemberNumber())
            .point(100L)
            .expireDate(LocalDateTime.now().plusDays(1))
            .unlimitedDate(true)
            .build();
      }
    }
  }

  @Nested
  @DisplayName("회원의 사용 가능한 적립금을 조회 할 때")
  class DescribeGetAvailableMemberPoint {

    void publishPoint(long memberNumber, long point, boolean unlimitedDate, LocalDateTime expireDate) {
      pointDomainService.publishPoint(PublishPointRequest.builder()
          .historyType(HistoryType.TYPE_1.getValue())
          .memberNumber(memberNumber)
          .point(point)
          .unlimitedDate(unlimitedDate)
          .expireDate(expireDate)
          .build());
    }

    List<Point> subject(long memberNumber) {
      return pointDomainService.getAvailableMemberPoint(memberNumber);
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료일 이전의 적립금이 있으면")
    class Context0 {
      private static final long MEMBER_NUMBER = 1019;

      @Test
      @DisplayName("해당 적립금을 반환 한다")
      void test() {
        given();
        List<Point> subject = subject(MEMBER_NUMBER);
        assertThat(subject.size()).isEqualTo(1);
        assertThat(subject.get(0).getRemain()).isEqualTo(1000);
      }

      void given() {
        publishPoint(MEMBER_NUMBER, 1000, false, LocalDateTime.now().plusDays(1));
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료일이 무제한인 적립금이 있으면")
    class Context1 {
      private static final long MEMBER_NUMBER = 1020;
      @Test
      @DisplayName("해당 적립금을 반환 한다")
      void test() {
        given();
        List<Point> subject = subject(MEMBER_NUMBER);
        assertThat(subject.size()).isEqualTo(1);
        assertThat(subject.get(0).getRemain()).isEqualTo(1000);
      }

      void given() {
        publishPoint(MEMBER_NUMBER, 1000, true, null);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료일이 지난 적립금이 있으면")
    class Context2 {
      private static final long MEMBER_NUMBER = 1021;

      @Test
      @DisplayName("적립금을 반환하지 않는다")
      void test() {
        given();
        List<Point> subject = subject(MEMBER_NUMBER);
        assertThat(subject).isEmpty();
      }

      void given() {
        publishPoint(MEMBER_NUMBER, 1000, false, LocalDateTime.now().minusDays(1));
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("적립금의 잔액이 없으면")
    class Context3 {
      private static final long MEMBER_NUMBER = 1018;

      @Test
      @DisplayName("적립금을 반환하지 않는다")
      void test() {
        given();
        List<Point> subject = subject(MEMBER_NUMBER);
        assertThat(subject).isEmpty();
      }

      void given() {
        publishPoint(MEMBER_NUMBER, 0, false, LocalDateTime.now().minusDays(1));
        publishPoint(MEMBER_NUMBER, 0, true, null);
      }
    }
  }

  @Nested
  @DisplayName("회원의 적립금을 사용 할 때")
  class DescribeConsumeMemberPoint {
    PointConsumeResult subject(long memberNumber, int amount) {
      return pointDomainService.consumeMemberPoint(memberNumber, amount);
    }

    int givenConsumeAmount() {
      return 10000;
    }

    long getMemberPoint(long memberNumber) {
      return pointDomainService.getAvailableMemberPoint(memberNumber)
          .stream().mapToLong(Point::getRemain).sum();
    }

    void publishPoint(long memberNumber, long amount) {
      pointDomainService.publishPoint(PublishPointRequest.builder()
          .memberNumber(memberNumber)
          .point(amount)
          .historyType(HistoryType.TYPE_1.getValue())
          .build());
    }

    @Nested
    @DisplayName("사용 가능 한 적립금이 없을 때")
    class Context0 {
      private static final long MEMBER_NUMBER = 1017;

      @Test
      @DisplayName("사용 된 적립금이 없어야 한다")
      void test() {
        PointConsumeResult pointConsumeResult = subject(MEMBER_NUMBER, givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(0);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("사용 가능 한 적립금이 일부만 있을 때")
    class Context1 {
      public static final long MEMBER_NUMBER = 1012;

      @Test
      @DisplayName("일부만 사용 한다")
      void test() {
        publishPoint(MEMBER_NUMBER, givenPublishPoint());
        PointConsumeResult pointConsumeResult = subject(MEMBER_NUMBER, givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(givenPublishPoint());
        assertThat(pointConsumeResult.getRemain())
            .isEqualTo(givenConsumeAmount() - givenPublishPoint());
        assertThat(getMemberPoint(MEMBER_NUMBER)).isEqualTo(0);
      }

      int givenPublishPoint() {
        return 3000;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("사용 가능 한 적립금이 초과하여 있을 때")
    class Context2 {
      public static final long MEMBER_NUMBER = 1013;

      @Test
      @DisplayName("요청한 적립금을 전부 사용하고 남은적립금이 조회 된다")
      void test() {
        publishPoint(MEMBER_NUMBER, givenPublishPoint());
        PointConsumeResult pointConsumeResult = subject(MEMBER_NUMBER, givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(givenConsumeAmount());
        assertThat(pointConsumeResult.getRemain()).isEqualTo(0);
        assertThat(getMemberPoint(MEMBER_NUMBER)).isEqualTo(givenPublishPoint() - givenConsumeAmount());
      }

      int givenPublishPoint() {
        return 15000;
      }
    }
  }

  @Nested
  @DisplayName("회원의 유상 적립금을 사용 할 때")
  class DescribeConsumeSettleMemberPoint {
    PointConsumeResult subject(long memberNumber, int amount) {
      return pointDomainService.consumeMemberPoint(memberNumber, amount, true);
    }

    int givenConsumeAmount() {
      return 10000;
    }

    long getMemberPoint(long memberNumber) {
      return pointDomainService.getAvailableMemberPoint(memberNumber)
          .stream().mapToLong(Point::getRemain).sum();
    }

    void publishCashPoint(long memberNumber, long amount) {
      pointDomainService.publishPoint(PublishPointRequest.builder()
          .memberNumber(memberNumber)
          .point(amount)
          .historyType(HistoryType.TYPE_1.getValue())
          .settle(true)
          .build());
    }

    void publishFreePoint(long memberNumber, long amount) {
      pointDomainService.publishPoint(PublishPointRequest.builder()
          .memberNumber(memberNumber)
          .point(amount)
          .historyType(HistoryType.TYPE_1.getValue())
          .build());
    }

    @TransactionalTest
    @Nested
    @DisplayName("사용 가능 한 유상 적립금이 없을 때")
    class Context0 {
      private static final long MEMBER_NUMBER = 1009;
      @Test
      @DisplayName("사용 된 적립금이 없어야 한다")
      void test() {
        publishFreePoint(MEMBER_NUMBER, givenConsumeAmount());
        PointConsumeResult pointConsumeResult = subject(MEMBER_NUMBER, givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(0);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("사용 가능 한 적립금이 일부만 있을 때")
    class Context1 {
      private static final long MEMBER_NUMBER = 1010;
      @Test
      @DisplayName("일부만 사용 하고 남은 무상적립금이 조회 된다")
      void test() {
        publishFreePoint(MEMBER_NUMBER, givenPublishPoint());
        publishCashPoint(MEMBER_NUMBER, givenPublishPoint());
        PointConsumeResult pointConsumeResult = subject(MEMBER_NUMBER, givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(givenPublishPoint());
        assertThat(pointConsumeResult.getRemain())
            .isEqualTo(givenConsumeAmount() - givenPublishPoint());
        assertThat(getMemberPoint(MEMBER_NUMBER)).isEqualTo(givenPublishPoint());
      }

      int givenPublishPoint() {
        return 3000;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("사용 가능 한 적립금이 초과하여 있을 때")
    class Context2 {
      private static final long MEMBER_NUMBER = 1011;
      @Test
      @DisplayName("요청한 적립금을 전부 사용하고 남은 유/무상 적립금이 조회 된다")
      void test() {
        publishFreePoint(MEMBER_NUMBER, givenPublishPoint());
        publishCashPoint(MEMBER_NUMBER, givenPublishPoint());
        PointConsumeResult pointConsumeResult = subject(MEMBER_NUMBER, givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(givenConsumeAmount());
        assertThat(pointConsumeResult.getRemain()).isEqualTo(0);
        assertThat(getMemberPoint(MEMBER_NUMBER))
            .isEqualTo(givenPublishPoint() + givenPublishPoint() - givenConsumeAmount());
      }

      int givenPublishPoint() {
        return 15000;
      }
    }
  }

  @Nested
  @DisplayName("특정 주문 적립금 우선 사용 할 때")
  class DescribeConsumeOrderPoint {
    Point givenOrderPoint(long memberNumber, long orderNumber) {
      return pointDomainService.publishPoint(PublishPointRequest.builder()
          .orderNumber(orderNumber)
          .memberNumber(memberNumber)
          .point(givenOrderPointAmount())
          .historyType(HistoryType.TYPE_1.getValue())
          .build());
    }

    long givenOrderPointAmount() {
      return 1000;
    }

    Point givenNonOrderPoint(long memberNumber) {
      return pointDomainService.publishPoint(PublishPointRequest.builder()
          .memberNumber(memberNumber)
          .point(givenNonOrderPointAmount())
          .historyType(HistoryType.TYPE_12.getValue())
          .build());
    }

    long givenNonOrderPointAmount() {
      return 2000;
    }

    PointConsumeResult subject(long memberNumber, long orderNumber, int amount) {
      return pointDomainService.consumeOrderPoint(memberNumber, orderNumber, amount);
    }

    @TransactionalTest
    @Nested
    @DisplayName("특정 주문 적립금이 충분하면")
    class Context0 {

      public static final long MEMBER_NUMBER = 1008;
      public static final long ORDER_NUMBER = 1008000;

      @DisplayName("해당 주문 적립금만 차감 된다")
      @Test
      void test() {
        Point orderPoint = givenOrderPoint(MEMBER_NUMBER, ORDER_NUMBER);
        Point nonOrderPoint = givenNonOrderPoint(MEMBER_NUMBER);
        PointConsumeResult subject = subject(MEMBER_NUMBER, ORDER_NUMBER, givenAmount());

        assertThat(subject.getTotalConsumed()).isEqualTo(givenAmount());
        assertThat(subject.getRemain()).isEqualTo(0);

        Point updatedOrder = pointRepository.findById(orderPoint.getSeq()).get();
        assertThat(updatedOrder.getRemain()).isEqualTo(0);
        Point updatedNonOrderPoint = pointRepository.findById(nonOrderPoint.getSeq()).get();
        assertThat(updatedNonOrderPoint.getRemain()).isEqualTo(givenNonOrderPointAmount());
      }

      int givenAmount() {
        return 1000;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("특정 주문 적립금이 부족하고 총 적립금이 충분하면")
    class Context1 {

      public static final long MEMBER_NUMBER = 1005;
      public static final long ORDER_NUMBER = 1005000;

      @Test
      @DisplayName("해당 주문 적립금이 차감 되고 다른 적립금에서 모자른 적립금이 차감 된다")
      @Transactional
      void test() {
        Point orderPoint = givenOrderPoint(MEMBER_NUMBER, ORDER_NUMBER);
        Point nonOrderPoint = givenNonOrderPoint(MEMBER_NUMBER);
        PointConsumeResult subject = subject(MEMBER_NUMBER, ORDER_NUMBER, givenAmount());

        assertThat(subject.getTotalConsumed()).isEqualTo(givenAmount());
        assertThat(subject.getRemain()).isEqualTo(0);

        Point updatedOrder = pointRepository.findById(orderPoint.getSeq()).get();
        assertThat(updatedOrder.getRemain()).isEqualTo(0);
        Point updatedNonOrderPoint = pointRepository.findById(nonOrderPoint.getSeq()).get();
        assertThat(updatedNonOrderPoint.getRemain())
            .isEqualTo(givenNonOrderPointAmount() - (givenAmount() - givenOrderPointAmount()));
      }

      int givenAmount() {
        return 1500;
      }
    }

    @Nested
    @Transactional
    @SpringBootTest
    @DisplayName("총 적립금이 부족하면")
    class Context2 {

      public static final long MEMBER_NUMBER = 1006;
      public static final long ORDER_NUMBER = 1006000;

      @Test
      @Transactional
      @DisplayName("모든 적립금을 차감하고 차감하지 못한 적립금을 리턴한다")
      void test() {
        Point orderPoint = givenOrderPoint(MEMBER_NUMBER, ORDER_NUMBER);
        Point nonOrderPoint = givenNonOrderPoint(MEMBER_NUMBER);
        PointConsumeResult subject = subject(MEMBER_NUMBER, ORDER_NUMBER, 4000);

        assertThat(subject.getTotalConsumed())
            .isEqualTo(givenOrderPointAmount() + givenNonOrderPointAmount());
        assertThat(subject.getRemain())
            .isEqualTo(givenAmount() - givenOrderPointAmount() - givenNonOrderPointAmount());

        Point updatedOrder = pointRepository.findById(orderPoint.getSeq()).get();
        assertThat(updatedOrder.getRemain()).isEqualTo(0);
        Point updatedNonOrderPoint = pointRepository.findById(nonOrderPoint.getSeq()).get();
        assertThat(updatedNonOrderPoint.getRemain()).isEqualTo(0);
      }

      int givenAmount() {
        return 4000;
      }
    }

  }

  @Nested
  @DisplayName("대출 적립금을 상환 할 때")
  class DescribeRepayPoint {
    void given(long memberNumber) {
      pointDomainService.publishPoint(PublishPointRequest.builder()
          .point(-givenDebtAmount())
          .historyType(HistoryType.TYPE_12.getValue())
          .memberNumber(memberNumber)
          .unlimitedDate(true)
          .build());
    }

    long givenDebtAmount() {
      return 1000;
    }

    PointConsumeResult subject(long memberNumber, int amount) {
      return pointDomainService.repayMemberPoint(memberNumber, amount);
    }

    @TransactionalTest
    @Nested
    @DisplayName("대출적립금보다 상환적립금이 많으면")
    class Context0 {
      @Test
      @DisplayName("모두 상환한다")
      void test() {
        given(1000);
        PointConsumeResult subject = subject(1000, givenAmount());
        assertThat(subject.getTotalConsumed()).isEqualTo(givenDebtAmount());
      }

      int givenAmount() {
        return 2000;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("상환적립금보다 대출적립금이 많으면")
    class Context1 {
      @Test
      @DisplayName("상환적립금 만큼 상환 한다")
      void test() {
        given(1001);
        PointConsumeResult subject = subject(1001, givenAmount());
        assertThat(subject.getTotalConsumed()).isEqualTo(givenAmount());
      }

      int givenAmount() {
        return 500;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("대출적립금과 상환적립금이 같으면")
    class Context2 {
      private static final long MEMBER_NUMBER = 1002;

      @Test
      @DisplayName("모두 상환한다")
      void test() {
        given(MEMBER_NUMBER);
        PointConsumeResult subject = subject(MEMBER_NUMBER, givenAmount());
        assertThat(subject.getTotalConsumed()).isEqualTo(givenDebtAmount());
      }

      int givenAmount() {
        return 1000;
      }
    }
  }

  @Nested
  @DisplayName("회원의 만료적립금을 조회 할 때")
  class DescribeGetExpiredMemberPoint {

    LocalDateTime givenExpiredDateTime() {
      return LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    }

    LocalDateTime givenNonExpiredDateTime() {
      return LocalDateTime.of(2020, 1, 3, 0, 0, 0);
    }

    List<Point> subject(long memberNumber) {
      return pointDomainService
          .getExpiredMemberPoint(memberNumber, givenExpiredTargetDateTime());
    }

    LocalDateTime givenExpiredTargetDateTime() {
      return LocalDateTime.of(2020, 1, 2, 0, 0, 0);
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료된 적립금이 있다면")
    class Context0 {
      private static final long MEMBER_NUMBER = 1025;

      @Test
      @DisplayName("만료된 적립금이 조회 된다")
      void test() {
        givenPoint(MEMBER_NUMBER);
        List<Point> expiredPoints = subject(MEMBER_NUMBER);
        assertThat(expiredPoints.size()).isEqualTo(1);
      }

      void givenPoint(long memberNumber) {
        pointDomainService.publishPoint(PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .expireDate(givenExpiredDateTime())
            .memberNumber(memberNumber)
            .point(100L)
            .build());
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료된 적립금이 없다면")
    class Context1 {
      private static final long MEMBER_NUMBER = 1026;

      @Test
      @DisplayName("적립금이 조회되지 않는다")
      void test() {
        givenPoint();
        List<Point> expiredPoints = subject(MEMBER_NUMBER);
        assertThat(expiredPoints.size()).isEqualTo(0);
      }

      void givenPoint() {
        pointDomainService.publishPoint(PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .expireDate(givenNonExpiredDateTime())
            .memberNumber(givenMemberNumber())
            .point(100L)
            .build());
      }
    }

  }

  @Nested
  @DisplayName("회원의 다음 만료일을 조회 할 때")
  class DescribeGetMemberPointNextExpireDate {

    LocalDateTime givenExpiredDateTime() {
      return LocalDateTime.of(2030, 1, 1, 0, 0, 0);
    }

    Optional<LocalDateTime> subject() {
      return pointDomainService.getMemberPointNextExpireDate(givenMemberNumber());
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료될 적립금이 있다면")
    class Context0 {
      @Test
      @DisplayName("다음 만료일을 포함하는 Optional을 리턴한다")
      void test() {
        givenPoint();
        Optional<LocalDateTime> expireTime = subject();
        assertThat(expireTime).isNotEmpty();
        assertThat(expireTime.get()).isEqualToIgnoringHours(givenExpiredDateTime());
      }

      void givenPoint() {
        pointDomainService.publishPoint(PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .expireDate(givenExpiredDateTime())
            .memberNumber(givenMemberNumber())
            .point(100L)
            .build());
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료될 적립금이 없다면")
    class Context1 {
      @Test
      @DisplayName("비어 있는 Optional을 리턴한다")
      void test() {
        Optional<LocalDateTime> expireTime = subject();
        assertThat(expireTime).isEmpty();
      }
    }
  }
}
