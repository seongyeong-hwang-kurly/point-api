package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@DisplayName("PointHistoryDomainServiceTest class")
class PointHistoryDomainServiceTest {
  long givenMemberNumber() {
    return 10003;
  }

  long givenOrderNumber() {
    return 10003000;
  }

  @Autowired
  PointHistoryDomainService pointHistoryDomainService;

  @Autowired
  PointDomainService pointDomainService;

  @Nested
  @DisplayName("적립금 이력을 입력 할 때")
  class DescribeInsertHistory {
    PointHistory subject(PointHistoryInsertRequest request) {
      return pointHistoryDomainService.insertHistory(request);
    }

    @Nested
    @DisplayName("pointSeq가 없다면")
    class Context0 {
      @Test
      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      void test() {
        PointHistoryInsertRequest given = givenExceptPointSeq();
        try {
          subject(given);
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }

      private PointHistoryInsertRequest givenExceptPointSeq() {
        return PointHistoryInsertRequest.builder()
            .amount(1000L)
            .historyType(HistoryType.TYPE_1.getValue())
            .build();
      }
    }

    @Nested
    @DisplayName("amount가 없다면")
    class Context1 {
      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        PointHistoryInsertRequest given = givenExceptAmount();
        try {
          subject(given);
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }

      private PointHistoryInsertRequest givenExceptAmount() {
        return PointHistoryInsertRequest.builder()
            .pointSeq(1L)
            .historyType(HistoryType.TYPE_1.getValue())
            .build();
      }
    }

    @Nested
    @DisplayName("historyType 없다면")
    class Context2 {
      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        PointHistoryInsertRequest given = givenExceptHistoryType();
        try {
          subject(given);
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }

      private PointHistoryInsertRequest givenExceptHistoryType() {
        return PointHistoryInsertRequest.builder()
            .pointSeq(1L)
            .amount(1000L)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("올바른 값이 입력 된다면")
    class Context3 {
      @DisplayName("입력하고 값을 리턴 한다")
      @Test
      @Transactional
      void test() {
        Point point = pointDomainService.publishPoint(PublishPointRequest.builder()
                .historyType(HistoryType.TYPE_1.getValue())
                .point(1000L)
                .memberNumber(givenMemberNumber())
                .orderNumber(1L)
                .build());

        PointHistoryInsertRequest given = given(point);
        PointHistory subject = subject(given);

        assertThat(subject.getSeq()).isNotZero();
        assertThat(subject.getPoint().getSeq()).isEqualTo(given.getPointSeq());
        assertThat(subject.getAmount()).isEqualTo(given.getAmount());
        assertThat(subject.getOrderNumber()).isEqualTo(given.getOrderNumber());
        assertThat(subject.getHistoryType()).isEqualTo(given.getHistoryType());
        assertThat(subject.getDetail()).isEqualTo(given.getDetail());
        assertThat(subject.getMemo()).isEqualTo(given.getMemo());
        assertThat(subject.isSettle()).isEqualTo(given.isSettle());
        assertThat(subject.getActionMemberNumber()).isEqualTo(given.getActionMemberNumber());
        assertThat(subject.getRegTime()).isEqualToIgnoringSeconds(LocalDateTime.now());

      }

      private PointHistoryInsertRequest given(Point point) {
        return PointHistoryInsertRequest.builder()
            .pointSeq(point.getSeq())
            .amount(1000L)
            .historyType(HistoryType.TYPE_1.getValue())
            .actionMemberNumber(givenMemberNumber())
            .detail("detail")
            .memo("memo")
            .orderNumber(1L)
            .settle(true)
            .build();
      }
    }
  }

  @Nested
  @DisplayName("적립금 이력을 조회 할 때")
  class DescribeGetHistory {

    Point given() {
      조회되지않아야하는이력();
      return 조회되어야하는이력();
    }

    void 조회되지않아야하는이력() {
      Point point = pointDomainService.publishPoint(PublishPointRequest.builder()
          .historyType(HistoryType.TYPE_1.getValue())
          .point(1000L)
          .memberNumber(givenMemberNumber())
          .orderNumber(1L)
          .build());
      pointHistoryDomainService.insertHistory(
          PointHistoryInsertRequest.builder()
              .orderNumber(1L)
              .historyType(HistoryType.TYPE_1.getValue())
              .pointSeq(point.getSeq())
              .amount(1000L)
              .build());
    }

    Point 조회되어야하는이력() {
      Point point = pointDomainService.publishPoint(PublishPointRequest.builder()
          .historyType(HistoryType.TYPE_1.getValue())
          .point(1000L)
          .memberNumber(givenMemberNumber())
          .orderNumber(givenOrderNumber())
          .build());
      pointHistoryDomainService.insertHistory(
          PointHistoryInsertRequest.builder()
              .orderNumber(givenOrderNumber())
              .historyType(HistoryType.TYPE_1.getValue())
              .pointSeq(point.getSeq())
              .amount(1000L)
              .build());

      return point;
    }

    @TransactionalTest
    @Nested
    @DisplayName("pointSeq로 조회 하면")
    class Context0 {

      @Test
      @Transactional
      @DisplayName("이력을 1건 리턴 한다")
      void test() {
        Point given = given();
        List<PointHistory> subject = subject(given.getSeq());
        assertThat(subject.size()).isEqualTo(1);
        assertThat(subject.get(0).getPoint().getSeq()).isEqualTo(given.getSeq());
      }

      List<PointHistory> subject(long pointSeq) {
        return pointHistoryDomainService.getByPointSeq(pointSeq);
      }
    }
  }

  @Nested
  @DisplayName("적립금 지급 이력을 조회 할 때")
  class DescribePublishHistory {
    @TransactionalTest
    @Nested
    @DisplayName("등록일로 조회 하면")
    class Context0 {
      void given() {
        givenPoint();
      }

      Point givenPoint() {
        Point point = pointDomainService.publishPoint(PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .point(1000L)
            .memberNumber(givenMemberNumber())
            .orderNumber(givenOrderNumber())
            .build());
        pointHistoryDomainService.insertHistory(
            PointHistoryInsertRequest.builder()
                .orderNumber(givenOrderNumber())
                .historyType(HistoryType.TYPE_1.getValue())
                .pointSeq(point.getSeq())
                .amount(1000L)
                .build());

        return point;
      }

      Page<PointHistory> subject(LocalDateTime from, LocalDateTime to) {
        return pointHistoryDomainService.getPublishedByRegTime(from, to, PageRequest.of(0, 10));
      }

      @Test
      @DisplayName("이력을 리턴 한다")
      void test() {
        given();
        Page<PointHistory> subject =
            subject(LocalDateTime.now().minusSeconds(1), LocalDateTime.now().plusSeconds(1));
        assertThat(subject.getTotalElements()).isEqualTo(1);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("이력 타입으로 조회 하면")
    class Context1 {
      void given() {
        givenPoint(HistoryType.TYPE_1.getValue());
        givenPoint(HistoryType.TYPE_2.getValue());
      }

      Point givenPoint(int historyType) {
        Point point = pointDomainService.publishPoint(PublishPointRequest.builder()
            .historyType(historyType)
            .point(1000L)
            .memberNumber(givenMemberNumber())
            .orderNumber(givenOrderNumber())
            .build());
        pointHistoryDomainService.insertHistory(
            PointHistoryInsertRequest.builder()
                .orderNumber(givenOrderNumber())
                .historyType(historyType)
                .pointSeq(point.getSeq())
                .amount(1000L)
                .build());

        return point;
      }

      Page<PointHistory> subject(List<Integer> historyTypes) {
        return pointHistoryDomainService.getPublishedByHistoryTypes(
            LocalDateTime.now().minusSeconds(1),
            LocalDateTime.now().plusSeconds(1),
            historyTypes,
            PageRequest.of(0, 10));
      }


      @Test
      @DisplayName("이력타입이 1인 이력을 리턴 한다")
      void test() {
        given();
        Page<PointHistory> subject =
            subject(Arrays.asList(HistoryType.TYPE_1.getValue()));
        assertThat(subject.getTotalElements()).isEqualTo(1);
      }

      @Test
      @DisplayName("이력타입이 2인 이력을 리턴 한다")
      void test1() {
        given();
        Page<PointHistory> subject =
            subject(Arrays.asList(HistoryType.TYPE_2.getValue()));
        assertThat(subject.getTotalElements()).isEqualTo(1);
      }

      @Test
      @DisplayName("이력타입이 1과 2인 이력을 리턴 한다")
      void test2() {
        given();
        Page<PointHistory> subject =
            subject(Arrays.asList(HistoryType.TYPE_1.getValue(), HistoryType.TYPE_2.getValue()));
        assertThat(subject.getTotalElements()).isEqualTo(2);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("지급자로 조회 하면")
    class Context2 {

      void given(long memberNumber) {
        givenPoint(memberNumber, HistoryType.TYPE_1.getValue(), memberNumber);
        givenPoint(memberNumber, HistoryType.TYPE_2.getValue(), memberNumber - 1);
      }

      Point givenPoint(long memberNumber, int historyType, long actionMemberNumber) {
        Point point = pointDomainService.publishPoint(PublishPointRequest.builder()
            .historyType(historyType)
            .point(1000L)
            .memberNumber(memberNumber)
            .orderNumber(givenOrderNumber())
            .actionMemberNumber(actionMemberNumber)
            .build());
        pointHistoryDomainService.insertHistory(
            PointHistoryInsertRequest.builder()
                .orderNumber(givenOrderNumber())
                .historyType(historyType)
                .actionMemberNumber(actionMemberNumber)
                .pointSeq(point.getSeq())
                .amount(1000L)
                .build());

        return point;
      }

      Page<PointHistory> subject(List<Long> actionMemberNumbers) {
        return pointHistoryDomainService.getPublishedByActionMemberNumbers(
            LocalDateTime.now().minusSeconds(1),
            LocalDateTime.now().plusSeconds(1),
            actionMemberNumbers,
            PageRequest.of(0, 10));
      }


      @Test
      @Transactional
      @DisplayName("지급자가 givenMemberNumber()인 이력을 리턴 한다")
      void test() {
        long MEMBER_NUMBER = 10271;
        given(MEMBER_NUMBER);
        Page<PointHistory> subject = subject(Arrays.asList(MEMBER_NUMBER));
        assertThat(subject.getTotalElements()).isEqualTo(1);
      }

      @Test
      @Transactional
      @DisplayName("지급자가 givenMemberNumber()-1인 이력을 리턴 한다")
      void test1() {
        long MEMBER_NUMBER = 10281;
        given(MEMBER_NUMBER);
        Page<PointHistory> subject = subject(Arrays.asList(MEMBER_NUMBER - 1));
        assertThat(subject.getTotalElements()).isEqualTo(1);
      }

      @Test
      @Transactional
      @DisplayName("이력타입이 givenMemberNumber()와 givenMemberNumber()-1인 이력을 리턴 한다")
      void test2() {
        long MEMBER_NUMBER = 10291;
        given(MEMBER_NUMBER);
        Page<PointHistory> subject =
            subject(Arrays.asList(MEMBER_NUMBER, MEMBER_NUMBER - 1));
        assertThat(subject.getTotalElements()).isEqualTo(2);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("이력 타입과 지급자로 조회 하면")
    class Context3 {
      void given() {
        조회되어야하는이력(givenMemberNumber());
        조회되어야하는이력(givenMemberNumber() - 1);
      }

      Point 조회되어야하는이력(long actionMemberNumber) {
        Point point = pointDomainService.publishPoint(PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .point(1000L)
            .memberNumber(givenMemberNumber())
            .orderNumber(givenOrderNumber())
            .actionMemberNumber(actionMemberNumber)
            .build());
        pointHistoryDomainService.insertHistory(
            PointHistoryInsertRequest.builder()
                .orderNumber(givenOrderNumber())
                .historyType(HistoryType.TYPE_1.getValue())
                .actionMemberNumber(actionMemberNumber)
                .pointSeq(point.getSeq())
                .amount(1000L)
                .build());

        return point;
      }

      Page<PointHistory> subject(List<Integer> historyTypes, List<Long> actionMemberNumbers) {
        return pointHistoryDomainService.getPublishedByHistoryTypesAndActionMemberNumbers(
            LocalDateTime.now().minusSeconds(1),
            LocalDateTime.now().plusSeconds(1),
            historyTypes,
            actionMemberNumbers,
            PageRequest.of(0, 10));
      }


      @Test
      @DisplayName("이력이 1이고 지급자가 givenMemberNumber()인 이력을 리턴 한다")
      void test() {
        given();
        Page<PointHistory> subject = subject(Arrays.asList(HistoryType.TYPE_1.getValue()),
            Arrays.asList(givenMemberNumber()));
        assertThat(subject.getTotalElements()).isEqualTo(1);
      }
    }
  }
}
