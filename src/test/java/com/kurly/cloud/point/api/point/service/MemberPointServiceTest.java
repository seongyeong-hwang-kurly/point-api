/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("MemberPointService class")
class MemberPointServiceTest {
  @Autowired
  MemberPointService memberPointService;

  @Autowired
  MemberPointRepository memberPointRepository;

  long givenMemberNumber() {
    return 999999999;
  }

  @AfterEach
  void clear() {
    memberPointRepository.deleteById(givenMemberNumber());
  }

  @Nested
  @DisplayName("적립금 정보를 생성할 때")
  class DescribeCreateMemberPoint {
    @Nested
    @DisplayName("적립금정보를 생성하면")
    class Context0 {
      int givenFreePoint() {
        return 100;
      }

      int givenCashPoint() {
        return 200;
      }

      int givenTotalPoint() {
        return givenFreePoint() + givenCashPoint();
      }

      MemberPoint subject() {
        return memberPointService.createMemberPoint(givenMemberNumber(), givenFreePoint(), givenCashPoint());
      }

      @Test
      @DisplayName("생성되어야 한다")
      void test() {
        MemberPoint subject = subject();
        assertThat(subject.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(subject.getTotalPoint()).isEqualTo(givenTotalPoint());
        assertThat(subject.getFreePoint()).isEqualTo(givenFreePoint());
        assertThat(subject.getCashPoint()).isEqualTo(givenCashPoint());
        assertThat(subject.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }
  }

  @Nested
  @DisplayName("적립금이 추가 또는 차감 될때")
  class DescribePlusMinusPoint {

    MemberPoint givenMemberPoint() {
      return memberPointService.createMemberPoint(givenMemberNumber(), givenFreePoint(), givenCashPoint());
    }

    int givenFreePoint() {
      return 100;
    }

    int givenCashPoint() {
      return 200;
    }

    @Nested
    @DisplayName("무상 적립금이 추가되면")
    class Context0 {
      MemberPoint subject(long memberNumber, int point) {
        return memberPointService.plusFreePoint(memberNumber, point);
      }

      @Test
      @DisplayName("무상 적립금과 총적립금이 증가 해야 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint = subject(givenMemberPoint.getMemberNumber(), givenFreePoint());
        assertThat(subjectMemberPoint.getMemberNumber()).isEqualTo(givenMemberPoint.getMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint()).isEqualTo(givenMemberPoint.getTotalPoint() + givenFreePoint());
        assertThat(subjectMemberPoint.getFreePoint()).isEqualTo(givenMemberPoint.getFreePoint() + givenFreePoint());
        assertThat(subjectMemberPoint.getCashPoint()).isEqualTo(givenMemberPoint.getCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }

    @Nested
    @DisplayName("유상 적립금이 추가되면")
    class Context1 {
      MemberPoint subject(long memberNumber, int point) {
        return memberPointService.plusCashPoint(memberNumber, point);
      }

      @Test
      @DisplayName("유상 적립금과 총적립금이 증가 해야 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint = subject(givenMemberPoint.getMemberNumber(), givenCashPoint());
        assertThat(subjectMemberPoint.getMemberNumber()).isEqualTo(givenMemberPoint.getMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint()).isEqualTo(givenMemberPoint.getTotalPoint() + givenCashPoint());
        assertThat(subjectMemberPoint.getFreePoint()).isEqualTo(givenMemberPoint.getFreePoint());
        assertThat(subjectMemberPoint.getCashPoint()).isEqualTo(givenMemberPoint.getCashPoint() + givenCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }

    @Nested
    @DisplayName("무상 적립금이 차감되면")
    class Context2 {
      MemberPoint subject(long memberNumber, int point) {
        return memberPointService.minusFreePoint(memberNumber, point);
      }

      @Test
      @DisplayName("무상 적립금과 총적립금이 감소 해야 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint = subject(givenMemberPoint.getMemberNumber(), givenFreePoint());
        assertThat(subjectMemberPoint.getMemberNumber()).isEqualTo(givenMemberPoint.getMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint()).isEqualTo(givenMemberPoint.getTotalPoint() - givenFreePoint());
        assertThat(subjectMemberPoint.getFreePoint()).isEqualTo(givenMemberPoint.getFreePoint() - givenFreePoint());
        assertThat(subjectMemberPoint.getCashPoint()).isEqualTo(givenMemberPoint.getCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }


    @Nested
    @DisplayName("유상 적립금이 차감되면")
    class Context3 {
      MemberPoint subject(long memberNumber, int point) {
        return memberPointService.minusCashPoint(memberNumber, point);
      }

      @Test
      @DisplayName("유상 적립금과 총적립금이 감소 해야 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint = subject(givenMemberPoint.getMemberNumber(), givenCashPoint());
        assertThat(subjectMemberPoint.getMemberNumber()).isEqualTo(givenMemberPoint.getMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint()).isEqualTo(givenMemberPoint.getTotalPoint() - givenCashPoint());
        assertThat(subjectMemberPoint.getFreePoint()).isEqualTo(givenMemberPoint.getFreePoint());
        assertThat(subjectMemberPoint.getCashPoint()).isEqualTo(givenMemberPoint.getCashPoint() - givenCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }
  }

  @Nested
  @DisplayName("적립금 정보를 조회할때")
  class DescribeGetMemberPoint {
    MemberPoint subject() {
      return memberPointService.getOrCrateMemberPoint(givenMemberNumber());
    }

    @Nested
    @DisplayName("회원의 적립금정보가 존재하지 않으면")
    class ContextWithNonExists {

      @Test
      @DisplayName("회원의 적립금 정보를 초기화 하여 생성 해야 한다")
      void test() {
        MemberPoint memberPoint = subject();
        assertThat(memberPoint.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(memberPoint.getTotalPoint()).isEqualTo(0);
        assertThat(memberPoint.getFreePoint()).isEqualTo(0);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
        assertThat(memberPoint.getUpdateTime()).isEqualToIgnoringSeconds(LocalDateTime.now());
      }
    }

    @Nested
    @DisplayName("회원의 적립금정보가 존재한다면")
    class ContextWithExists {
      MemberPoint givenMemberPoint() {
        return memberPointService.createMemberPoint(givenMemberNumber(), 100, 200);
      }

      @Test
      @DisplayName("회원의 적립금이 조회 되어야 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint = subject();
        assertThat(subjectMemberPoint.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint()).isEqualTo(givenMemberPoint.getTotalPoint());
        assertThat(subjectMemberPoint.getFreePoint()).isEqualTo(givenMemberPoint.getFreePoint());
        assertThat(subjectMemberPoint.getCashPoint()).isEqualTo(givenMemberPoint.getCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime()).isEqualToIgnoringNanos(givenMemberPoint.getUpdateTime());
      }
    }
  }
}
