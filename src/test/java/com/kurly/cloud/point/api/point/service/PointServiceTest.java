/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
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
@DisplayName("PointService class")
class PointServiceTest {

  @Autowired
  PointService pointService;

  long givenMemberNumber() {
    return 999999999;
  }

  @Nested
  @DisplayName("적립금을 회원에게 지급 할 때")
  class DescribePublishPoint {
    Point subject(PublishPointRequest request) {
      return pointService.publishPoint(request);
    }

    @Nested
    @DisplayName("memberNumber가 없으면")
    class Context0 {

      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .point(100)
            .build();
      }

      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }
    }

    @Nested
    @DisplayName("point가 없으면")
    class Context1 {
      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .memberNumber(givenMemberNumber())
            .build();
      }

      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }
    }

    @Nested
    @DisplayName("historyType이 없으면")
    class Context2 {
      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .memberNumber(givenMemberNumber())
            .point(100)
            .build();
      }

      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        try {
          subject(givenRequest());
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("모든값이 존재한다면")
    class Context3 {
      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .memberNumber(givenMemberNumber())
            .point(100)
            .build();
      }

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
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료일을 지정하면")
    class Context4 {
      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .memberNumber(givenMemberNumber())
            .point(100)
            .expireDate(LocalDateTime.now().plusDays(1))
            .build();
      }

      @DisplayName("지정한 만료일의 시각이 23:59:59 으로 입력 한다")
      @Test
      void test() {
        PublishPointRequest given = givenRequest();
        Point subject = subject(given);

        assertThat(subject.getExpireTime())
            .isEqualTo(given.getExpireDate().withHour(23).withMinute(59).withSecond(59));
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료일의 제한이 없다면")
    class Context5 {
      PublishPointRequest givenRequest() {
        return PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .memberNumber(givenMemberNumber())
            .point(100)
            .expireDate(LocalDateTime.now().plusDays(1))
            .unlimitedDate(true)
            .build();
      }

      @DisplayName("만료일은 NULL을 리턴한다")
      @Test
      void test() {
        PublishPointRequest given = givenRequest();
        Point subject = subject(given);

        assertThat(subject.getExpireTime()).isNull();
      }
    }
  }

  @Nested
  @DisplayName("회원의 사용 가능한 적립금을 조회 할 때")
  class DescribeGetAvailableMemberPoint {

    void publishPoint(int point, boolean unlimitedDate, LocalDateTime expireDate) {
      pointService.publishPoint(PublishPointRequest.builder()
          .historyType(HistoryType.TYPE_1.getValue())
          .memberNumber(givenMemberNumber())
          .point(point)
          .unlimitedDate(unlimitedDate)
          .expireDate(expireDate)
          .build());
    }

    List<Point> subject() {
      return pointService.getAvailableMemberPoint(givenMemberNumber());
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료일 이전의 포인트가 있으면")
    class Context0 {
      void given() {
        publishPoint(1000, false, LocalDateTime.now().plusDays(1));
      }

      @Test
      @DisplayName("해당 포인트를 반환 한다")
      void test() {
        given();
        List<Point> subject = subject();
        assertThat(subject.size()).isEqualTo(1);
        assertThat(subject.get(0).getRemain()).isEqualTo(1000);
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료일이 무제한인 포인트가 있으면")
    class Context1 {
      void given() {
        publishPoint(1000, true, null);
      }

      @Test
      @DisplayName("해당 포인트를 반환 한다")
      void test() {
        given();
        List<Point> subject = subject();
        assertThat(subject.size()).isEqualTo(1);
        assertThat(subject.get(0).getRemain()).isEqualTo(1000);
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료일이 지난 포인트가 있으면")
    class Context2 {
      void given() {
        publishPoint(1000, false, LocalDateTime.now().minusDays(1));
      }

      @Test
      @DisplayName("포인트를 반환하지 않는다")
      void test() {
        given();
        List<Point> subject = subject();
        assertThat(subject).isEmpty();
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("포인트의 잔액이 없으면")
    class Context3 {
      void given() {
        publishPoint(0, false, LocalDateTime.now().minusDays(1));
        publishPoint(0, true, null);
      }

      @Test
      @DisplayName("포인트를 반환하지 않는다")
      void test() {
        given();
        List<Point> subject = subject();
        assertThat(subject).isEmpty();
      }
    }
  }

  @Nested
  @DisplayName("회원의 적립금을 사용 할 때")
  class DescribeConsumeMemberPoint {
    PointConsumeResult subject(int amount) {
      return pointService.consumeMemberPoint(givenMemberNumber(), amount);
    }

    int givenConsumeAmount() {
      return 10000;
    }

    int getMemberPoint() {
      return pointService.getAvailableMemberPoint(givenMemberNumber())
          .stream().mapToInt(Point::getRemain).sum();
    }

    void publishPoint(int amount) {
      pointService.publishPoint(PublishPointRequest.builder()
          .memberNumber(givenMemberNumber())
          .point(amount)
          .historyType(HistoryType.TYPE_1.getValue())
          .build());
    }

    @Nested
    @DisplayName("사용 가능 한 적립금이 없을 때")
    class Context0 {
      @Test
      @DisplayName("사용 된 적립금이 없어야 한다")
      void test() {
        PointConsumeResult pointConsumeResult = subject(givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(0);
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("사용 가능 한 적립금이 일부만 있을 때")
    class Context1 {
      int givenPublishPoint() {
        return 3000;
      }

      @Test
      @DisplayName("일부만 사용 한다")
      void test() {
        publishPoint(givenPublishPoint());
        PointConsumeResult pointConsumeResult = subject(givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(givenPublishPoint());
        assertThat(pointConsumeResult.getRemain())
            .isEqualTo(givenConsumeAmount() - givenPublishPoint());
        assertThat(getMemberPoint()).isEqualTo(0);
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("사용 가능 한 적립금이 초과하여 있을 때")
    class Context2 {
      int givenPublishPoint() {
        return 15000;
      }

      @Test
      @DisplayName("요청한 포인트를 전부 사용하고 남은포인트가 조회 된다")
      void test() {
        publishPoint(givenPublishPoint());
        PointConsumeResult pointConsumeResult = subject(givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(givenConsumeAmount());
        assertThat(pointConsumeResult.getRemain()).isEqualTo(0);
        assertThat(getMemberPoint()).isEqualTo(givenPublishPoint() - givenConsumeAmount());
      }
    }
  }

  @Nested
  @DisplayName("회원의 유상 적립금을 사용 할 때")
  class DescribeConsumeSettleMemberPoint {
    PointConsumeResult subject(int amount) {
      return pointService.consumeMemberPoint(givenMemberNumber(), amount, true);
    }

    int givenConsumeAmount() {
      return 10000;
    }

    int getMemberPoint() {
      return pointService.getAvailableMemberPoint(givenMemberNumber())
          .stream().mapToInt(Point::getRemain).sum();
    }

    void publishCashPoint(int amount) {
      pointService.publishPoint(PublishPointRequest.builder()
          .memberNumber(givenMemberNumber())
          .point(amount)
          .historyType(HistoryType.TYPE_1.getValue())
          .settle(true)
          .build());
    }

    void publishFreePoint(int amount) {
      pointService.publishPoint(PublishPointRequest.builder()
          .memberNumber(givenMemberNumber())
          .point(amount)
          .historyType(HistoryType.TYPE_1.getValue())
          .build());
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("사용 가능 한 유상 적립금이 없을 때")
    class Context0 {
      @Test
      @DisplayName("사용 된 적립금이 없어야 한다")
      void test() {
        publishFreePoint(givenConsumeAmount());
        PointConsumeResult pointConsumeResult = subject(givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(0);
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("사용 가능 한 적립금이 일부만 있을 때")
    class Context1 {
      int givenPublishPoint() {
        return 3000;
      }

      @Test
      @DisplayName("일부만 사용 하고 남은 무상적립금이 조회 된다")
      void test() {
        publishFreePoint(givenPublishPoint());
        publishCashPoint(givenPublishPoint());
        PointConsumeResult pointConsumeResult = subject(givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(givenPublishPoint());
        assertThat(pointConsumeResult.getRemain())
            .isEqualTo(givenConsumeAmount() - givenPublishPoint());
        assertThat(getMemberPoint()).isEqualTo(givenPublishPoint());
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("사용 가능 한 적립금이 초과하여 있을 때")
    class Context2 {
      int givenPublishPoint() {
        return 15000;
      }

      @Test
      @DisplayName("요청한 포인트를 전부 사용하고 남은 유/무상 포인트가 조회 된다")
      void test() {
        publishFreePoint(givenPublishPoint());
        publishCashPoint(givenPublishPoint());
        PointConsumeResult pointConsumeResult = subject(givenConsumeAmount());
        assertThat(pointConsumeResult.getTotalConsumed()).isEqualTo(givenConsumeAmount());
        assertThat(pointConsumeResult.getRemain()).isEqualTo(0);
        assertThat(getMemberPoint())
            .isEqualTo(givenPublishPoint() + givenPublishPoint() - givenConsumeAmount());
      }
    }
  }

  @Nested
  @DisplayName("특정 주문 적립금 우선 사용 할 때")
  class DescribeConsumeOrderPoint {
    long givenOrderNumber() {
      return 88888888;
    }

    int givenOrderPointAmount() {
      return 1000;
    }

    int givenNonOrderPointAmount() {
      return 2000;
    }

    Point givenOrderPoint() {
      return pointService.publishPoint(PublishPointRequest.builder()
          .orderNumber(givenOrderNumber())
          .memberNumber(givenMemberNumber())
          .point(givenOrderPointAmount())
          .historyType(HistoryType.TYPE_1.getValue())
          .build());
    }

    Point givenNonOrderPoint() {
      return pointService.publishPoint(PublishPointRequest.builder()
          .memberNumber(givenMemberNumber())
          .point(givenNonOrderPointAmount())
          .historyType(HistoryType.TYPE_12.getValue())
          .build());
    }

    PointConsumeResult subject(int amount) {
      return pointService.consumeOrderPoint(givenMemberNumber(), givenOrderNumber(), amount);
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("특정 주문 적립금이 충분하면")
    class Context0 {
      int givenAmount() {
        return 1000;
      }

      @DisplayName("해당 주문 적립금만 차감 된다")
      @Test
      void test() {
        Point orderPoint = givenOrderPoint();
        Point nonOrderPoint = givenNonOrderPoint();
        PointConsumeResult subject = subject(givenAmount());

        assertThat(subject.getTotalConsumed()).isEqualTo(givenAmount());
        assertThat(subject.getRemain()).isEqualTo(0);

        assertThat(orderPoint.getRemain()).isEqualTo(0);
        assertThat(nonOrderPoint.getRemain()).isEqualTo(givenNonOrderPointAmount());
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("특정 주문 적립금이 부족하고 총 적립금이 충분하면")
    class Context1 {
      int givenAmount() {
        return 1500;
      }

      @DisplayName("해당 주문 적립금이 차감 되고 다른 적립금에서 모자른 적립금이 차감 된다")
      @Test
      void test() {
        Point orderPoint = givenOrderPoint();
        Point nonOrderPoint = givenNonOrderPoint();
        PointConsumeResult subject = subject(givenAmount());

        assertThat(subject.getTotalConsumed()).isEqualTo(givenAmount());
        assertThat(subject.getRemain()).isEqualTo(0);

        assertThat(orderPoint.getRemain()).isEqualTo(0);
        assertThat(nonOrderPoint.getRemain())
            .isEqualTo(givenNonOrderPointAmount() - (givenAmount() - givenOrderPointAmount()));
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("총 적립금이 부족하면")
    class Context2 {
      int givenAmount() {
        return 4000;
      }

      @DisplayName("모든 적립금을 차감하고 차감하지 못한 적립금을 리턴한다")
      @Test
      void test() {
        Point orderPoint = givenOrderPoint();
        Point nonOrderPoint = givenNonOrderPoint();
        PointConsumeResult subject = subject(givenAmount());

        assertThat(subject.getTotalConsumed())
            .isEqualTo(givenOrderPointAmount() + givenNonOrderPointAmount());
        assertThat(subject.getRemain())
            .isEqualTo(givenAmount() - givenOrderPointAmount() - givenNonOrderPointAmount());

        assertThat(orderPoint.getRemain()).isEqualTo(0);
        assertThat(nonOrderPoint.getRemain()).isEqualTo(0);
      }
    }

  }

  @Nested
  @DisplayName("대출 포인트를 상환 할 때")
  class DescribeRepayPoint {
    int givenDebtAmount() {
      return 1000;
    }

    void given() {
      pointService.publishPoint(PublishPointRequest.builder()
          .point(-givenDebtAmount())
          .historyType(HistoryType.TYPE_12.getValue())
          .memberNumber(givenMemberNumber())
          .unlimitedDate(true)
          .build());
    }

    PointConsumeResult subject(int amount) {
      return pointService.repayMemberPoint(givenMemberNumber(), amount);
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("대출포인트보다 상환포인트가 많으면")
    class Context0 {
      int givenAmount() {
        return 2000;
      }

      @Test
      @DisplayName("모두 상환한다")
      void test() {
        given();
        PointConsumeResult subject = subject(givenAmount());
        assertThat(subject.getTotalConsumed()).isEqualTo(givenDebtAmount());
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("상환포인트보다 대출포인트가 많으면")
    class Context1 {
      int givenAmount() {
        return 500;
      }

      @Test
      @DisplayName("상환포인트 만큼 상환 한다")
      void test() {
        given();
        PointConsumeResult subject = subject(givenAmount());
        assertThat(subject.getTotalConsumed()).isEqualTo(givenAmount());
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("대출포인트와 상환포인트가 같으면")
    class Context2 {
      int givenAmount() {
        return 1000;
      }

      @Test
      @DisplayName("모두 상환한다")
      void test() {
        given();
        PointConsumeResult subject = subject(givenAmount());
        assertThat(subject.getTotalConsumed()).isEqualTo(givenDebtAmount());
      }
    }
  }

  @Nested
  @DisplayName("회원의 만료포인트를 조회 할 때")
  class DescribeGetExpiredMemberPoint {

    LocalDateTime givenExpiredDateTime() {
      return LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    }

    LocalDateTime givenExpiredTargetDateTime() {
      return LocalDateTime.of(2020, 1, 2, 0, 0, 0);
    }

    LocalDateTime givenNonExpiredDateTime() {
      return LocalDateTime.of(2020, 1, 3, 0, 0, 0);
    }

    List<Point> subject() {
      return pointService.getExpiredMemberPoint(givenMemberNumber(), givenExpiredTargetDateTime());
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료된 포인트가 있다면")
    class Context0 {
      void givenPoint() {
        pointService.publishPoint(PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .expireDate(givenExpiredDateTime())
            .memberNumber(givenMemberNumber())
            .point(100)
            .build());
      }

      @Test
      @DisplayName("만료된 포인트가 조회 된다")
      void test() {
        givenPoint();
        List<Point> expiredPoints = subject();
        assertThat(expiredPoints.size()).isEqualTo(1);
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료된 포인트가 없다면")
    class Context1 {
      void givenPoint() {
        pointService.publishPoint(PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .expireDate(givenNonExpiredDateTime())
            .memberNumber(givenMemberNumber())
            .point(100)
            .build());
      }

      @Test
      @DisplayName("포인트가 조회되지 않는다")
      void test() {
        givenPoint();
        List<Point> expiredPoints = subject();
        assertThat(expiredPoints.size()).isEqualTo(0);
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
      return pointService.getMemberPointNextExpireDate(givenMemberNumber());
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료될 포인트가 있다면")
    class Context0 {
      void givenPoint() {
        pointService.publishPoint(PublishPointRequest.builder()
            .historyType(HistoryType.TYPE_1.getValue())
            .expireDate(givenExpiredDateTime())
            .memberNumber(givenMemberNumber())
            .point(100)
            .build());
      }

      @Test
      @DisplayName("다음 만료일을 포함하는 Optional을 리턴한다")
      void test() {
        givenPoint();
        Optional<LocalDateTime> expireTime = subject();
        assertThat(expireTime).isNotEmpty();
        assertThat(expireTime.get()).isEqualToIgnoringHours(givenExpiredDateTime());
      }
    }

    @SpringBootTest
    @Transactional
    @Nested
    @DisplayName("만료될 포인트가 없다면")
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
