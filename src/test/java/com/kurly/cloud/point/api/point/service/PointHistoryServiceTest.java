package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("PointHistoryService class")
class PointHistoryServiceTest implements CommonTestGiven {

  @Autowired
  PointHistoryService pointHistoryService;

  @Autowired
  PointService pointService;

  @Nested
  @DisplayName("적립금 이력을 입력 할 때")
  class DescribeInsertHistory {
    PointHistory subject(PointHistoryInsertRequest request) {
      return pointHistoryService.insertHistory(request);
    }

    @Nested
    @DisplayName("pointSeq가 없다면")
    class Context0 {
      private PointHistoryInsertRequest givenExceptPointSeq() {
        return PointHistoryInsertRequest.builder()
            .amount(1000)
            .historyType(HistoryType.TYPE_1.getValue())
            .build();
      }

      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        PointHistoryInsertRequest given = givenExceptPointSeq();
        try {
          subject(given);
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }
    }

    @Nested
    @DisplayName("amount가 없다면")
    class Context1 {
      private PointHistoryInsertRequest givenExceptAmount() {
        return PointHistoryInsertRequest.builder()
            .pointSeq(1L)
            .historyType(HistoryType.TYPE_1.getValue())
            .build();
      }

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
    }

    @Nested
    @DisplayName("historyType 없다면")
    class Context2 {
      private PointHistoryInsertRequest givenExceptHistoryType() {
        return PointHistoryInsertRequest.builder()
            .pointSeq(1L)
            .amount(1000)
            .build();
      }

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
    }

    @TransactionalTest
    @Nested
    @DisplayName("올바른 값이 입력 된다면")
    class Context3 {
      private PointHistoryInsertRequest given() {
        return PointHistoryInsertRequest.builder()
            .pointSeq(1L)
            .amount(1000)
            .historyType(HistoryType.TYPE_1.getValue())
            .actionMemberNumber(givenMemberNumber())
            .detail("detail")
            .memo("memo")
            .orderNumber(1L)
            .settle(true)
            .build();
      }

      @DisplayName("입력하고 값을 리턴 한다")
      @Test
      void test() {
        PointHistoryInsertRequest given = given();
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
    }
  }

  @Nested
  @DisplayName("적립금 이력을 조회 할 때")
  class DescribeGetHistory {

    void 조회되지않아야하는이력() {
      Point point = pointService.publishPoint(PublishPointRequest.builder()
          .historyType(HistoryType.TYPE_1.getValue())
          .point(1000)
          .memberNumber(givenMemberNumber())
          .orderNumber(1L)
          .build());
      pointHistoryService.insertHistory(
          PointHistoryInsertRequest.builder()
              .orderNumber(1L)
              .historyType(HistoryType.TYPE_1.getValue())
              .pointSeq(point.getSeq())
              .amount(1000)
              .build());
    }

    Point 조회되어야하는이력() {
      Point point = pointService.publishPoint(PublishPointRequest.builder()
          .historyType(HistoryType.TYPE_1.getValue())
          .point(1000)
          .memberNumber(givenMemberNumber())
          .orderNumber(givenOrderNumber())
          .build());
      pointHistoryService.insertHistory(
          PointHistoryInsertRequest.builder()
              .orderNumber(givenOrderNumber())
              .historyType(HistoryType.TYPE_1.getValue())
              .pointSeq(point.getSeq())
              .amount(1000)
              .build());

      return point;
    }

    Point given() {
      조회되지않아야하는이력();
      return 조회되어야하는이력();
    }

    @TransactionalTest
    @Nested
    @DisplayName("pointSeq로 조회 하면")
    class Context0 {

      List<PointHistory> subject(long pointSeq) {
        return pointHistoryService.getByPointSeq(pointSeq);
      }

      @Test
      @DisplayName("이력을 1건 리턴 한다")
      void test() {
        Point given = given();
        List<PointHistory> subject = subject(given.getSeq());
        assertThat(subject.size()).isEqualTo(1);
        assertThat(subject.get(0).getPoint().getSeq()).isEqualTo(given.getSeq());
      }
    }


    @TransactionalTest
    @Nested
    @DisplayName("orderNumber로 조회 하면")
    class Context1 {
      List<PointHistory> subject() {
        return pointHistoryService.getPublishedByOrderNumber(givenOrderNumber());
      }

      @Test
      @DisplayName("이력을 1건 리턴 한다")
      void test() {
        given();
        List<PointHistory> subject = subject();
        assertThat(subject.size()).isEqualTo(1);
        assertThat(subject.get(0).getOrderNumber()).isEqualTo(givenOrderNumber());
      }
    }

  }
}
