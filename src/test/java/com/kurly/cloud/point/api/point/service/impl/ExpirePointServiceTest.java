package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.PointExpireResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import com.kurly.cloud.point.api.point.repository.MemberPointHistoryRepository;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import com.kurly.cloud.point.api.point.repository.PointHistoryRepository;
import com.kurly.cloud.point.api.point.repository.PointRepository;
import com.kurly.cloud.point.api.point.service.ExpirePointUseCase;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@DisplayName("ExpirePointServiceTest class")
public class ExpirePointServiceTest implements CommonTestGiven {

  @Autowired
  private ExpirePointUseCase expirePointUseCase;

  @Autowired
  private PublishPointUseCase publishPointUseCase;

  @Autowired
  private PointHistoryDomainService pointHistoryDomainService;

  @Autowired
  private MemberPointRepository memberPointRepository;

  @Autowired
  private MemberPointHistoryRepository memberPointHistoryRepository;

  @Autowired
  private PointRepository pointRepository;

  @Autowired
  private PointHistoryRepository pointHistoryRepository;

  @Nested
  @DisplayName("회원의 적립금을 만료 시킬 때")
  class DescribeExpireMemberPoint {

    LocalDateTime givenExpiredDateTime() {
      return LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    }

    LocalDateTime givenNonExpiredDateTime() {
      return LocalDateTime.of(2020, 1, 3, 0, 0, 0);
    }

    PointExpireResult expireByDate(LocalDateTime expiringExecutionDate) {
      return expirePointUseCase
          .expireMemberPoint(givenMemberNumber(), givenExpiredTargetDateTime());
    }

    LocalDateTime givenTargetDateTime() {
      return LocalDateTime.of(2020, 1, 2, 0, 0, 0);
    }

    void givenPoint(LocalDateTime expireDate) {
      publishPointUseCase.publish(PublishPointRequest.builder()
          .point(givenPointAmount())
          .memberNumber(givenMemberNumber())
          .historyType(HistoryType.TYPE_12.getValue())
          .actionMemberNumber(givenMemberNumber())
          .expireDate(expireDate)
          .detail("지급")
          .build());
    }

    long givenPointAmount() {
      return 1000L;
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료된 적립금이 있다면")
    class Context0 {

      @DisplayName("적립금이 만료처리 되고 만료처리 날짜가 expiredAt에 기입된다.")
      @Test
      public void test() {
        givenPoint(givenExpiredDateTime());
        PointExpireResult pointExpireResult = expireByDate(givenTargetDateTime());

        assertThat(pointExpireResult.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(pointExpireResult.getTotalExpired()).isEqualTo(givenPointAmount());

        List<MemberPoint> memberPoints = memberPointRepository.findAllByMemberNumber(givenMemberNumber());
        memberPoints.forEach(it->checkExpiredDate(it.getExpiredAt()));

        checkExpiredDate(getLatestMemberPointHistoryExpiredAt());

        List<Point> points = pointRepository.findAllByMemberNumber(givenMemberNumber());
        points.forEach(it-> checkExpiredDate(it.getExpiredAt()));

        checkExpiredDate(getLatestPointHistoriesExpiredAt(points));
      }

      private LocalDateTime getLatestPointHistoriesExpiredAt(List<Point> points) {
        return points.stream()
                .flatMap(it->pointHistoryRepository.findAllByPointSeq(it.getSeq()).stream())
                .filter(it -> it.getExpiredAt() != null)
                .max(Comparator.comparing(PointHistory::getExpiredAt))
                .orElseThrow().getExpiredAt();
      }

      private LocalDateTime getLatestMemberPointHistoryExpiredAt() {
        return memberPointHistoryRepository.findAllByMemberNumber(givenMemberNumber()).stream()
                .filter(it -> it.getExpiredAt() != null)
                .max(Comparator.comparing(MemberPointHistory::getExpiredAt))
                .orElseThrow().getExpiredAt();
      }

      private void checkExpiredDate(LocalDateTime it) {
        Assertions.assertEquals(it, withEndOfDate(givenExpiredDateTime()));
      }
    }

    private List<LocalDateTime> getDatesOfExpiredPoints(PointExpireResult pointExpireResult) {
      return pointExpireResult.getExpiredPointSeq().stream()
              .flatMap(it -> pointHistoryDomainService.getByPointSeq(it).stream())
              .map(PointHistory::getRegTime)
              .filter(Objects::nonNull)
              .collect(Collectors.toList());
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료된 적립금이 없다면")
    class Context1 {
      @DisplayName("적립금이 만료처리 되지않는다")
      @Test
      public void test() {
        givenPoint(givenNonExpiredDateTime());
        PointExpireResult pointExpireResult = expireByDate(givenTargetDateTime());

        assertThat(pointExpireResult.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(pointExpireResult.getTotalExpired()).isEqualTo(0);
        List<LocalDateTime> dates = getDatesOfExpiredPoints(pointExpireResult);
        assertEquals(0, dates.size());
      }
    }
  }
}
