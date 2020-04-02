package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.HistoryType;
import com.kurly.cloud.point.api.point.domain.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.domain.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.service.port.in.PublishPointPort;
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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("PublishPointService class")
class PublishPointServiceTest {
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

  long givenMemberNumber() {
    return 999999999;
  }

  @Nested
  @DisplayName("주문 적립 포인트를 발행 할 때")
  class DescribePublishByOrder {
    private PublishPointRequest given() {
      return PublishPointRequest.builder()
          .orderNumber(1585650564881L)
          .pointRatio(7)
          .point(1000)
          .memberNumber(givenMemberNumber())
          .build();
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("올바른 값이 입력되면")
    class Context0 {
      @Test
      @DisplayName("주문번호로 무상 포인트가 발행된다")
      void test() throws AlreadyPublishedException {
        PublishPointRequest request = given();
        publishPointPort.publishByOrder(request);
        MemberPoint memberPoint = memberPointService.getOrCrateMemberPoint(givenMemberNumber());

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

        List<Point> availableMemberPoint = pointService.getAvailableMemberPoint(givenMemberNumber());
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

        List<PointHistory> byOrderNumber = pointHistoryService
            .getByOrderNumber(request.getOrderNumber());
        assertThat(byOrderNumber.size()).isEqualTo(1);

      }
    }

    @SpringBootTest
    @Transactional
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
  @DisplayName("관리자(시스템)가 포인트를 발행 할 때")
  class DescribePublish {
    private PublishPointRequest given() {
      return PublishPointRequest.builder()
          .point(1000)
          .memberNumber(givenMemberNumber())
          .historyType(HistoryType.TYPE_12.getValue())
          .actionMemberNumber(givenMemberNumber())
          .detail("추천 지급")
          .memo("내맘대로")
          .build();
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("올바른 값이 입력되면")
    class Context0 {
      @Test
      @DisplayName("포인트가 발행된다")
      void test() {
        PublishPointRequest request = given();
        publishPointPort.publish(request);
        MemberPoint memberPoint = memberPointService.getOrCrateMemberPoint(givenMemberNumber());
        assertThat(memberPoint.getTotalPoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getFreePoint()).isEqualTo(request.getPoint());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }

  }
}
