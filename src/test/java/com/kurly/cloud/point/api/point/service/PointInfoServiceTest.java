/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.entity.PointInfo;
import com.kurly.cloud.point.api.point.repository.PointInfoRepository;
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
@DisplayName("PointInfoService class")
class PointInfoServiceTest {
  @Autowired
  PointInfoService pointInfoService;

  @Autowired
  PointInfoRepository pointInfoRepository;

  long givenMemberNumber() {
    return 999999999;
  }

  @AfterEach
  void clear() {
    pointInfoRepository.deleteById(givenMemberNumber());
  }

  @Nested
  @DisplayName("적립금 정보를 생성할 때")
  class DescribeCreatePointInfo {
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

      PointInfo subject() {
        return pointInfoService.createPointInfo(givenMemberNumber(), givenFreePoint(), givenCashPoint());
      }

      @Test
      @DisplayName("생성되어야 한다")
      void test() {
        PointInfo subject = subject();
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

    PointInfo givenPointInfo() {
      return pointInfoService.createPointInfo(givenMemberNumber(), givenFreePoint(), givenCashPoint());
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
      PointInfo subject(long memberNumber, int point) {
        return pointInfoService.plusFreePoint(memberNumber, point);
      }

      @Test
      @DisplayName("무상 적립금과 총적립금이 증가 해야 한다")
      void test() {
        PointInfo givenPointInfo = givenPointInfo();
        PointInfo subjectPointInfo = subject(givenPointInfo.getMemberNumber(), givenFreePoint());
        assertThat(subjectPointInfo.getMemberNumber()).isEqualTo(givenPointInfo.getMemberNumber());
        assertThat(subjectPointInfo.getTotalPoint()).isEqualTo(givenPointInfo.getTotalPoint() + givenFreePoint());
        assertThat(subjectPointInfo.getFreePoint()).isEqualTo(givenPointInfo.getFreePoint() + givenFreePoint());
        assertThat(subjectPointInfo.getCashPoint()).isEqualTo(givenPointInfo.getCashPoint());
        assertThat(subjectPointInfo.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }

    @Nested
    @DisplayName("유상 적립금이 추가되면")
    class Context1 {
      PointInfo subject(long memberNumber, int point) {
        return pointInfoService.plusCashPoint(memberNumber, point);
      }

      @Test
      @DisplayName("유상 적립금과 총적립금이 증가 해야 한다")
      void test() {
        PointInfo givenPointInfo = givenPointInfo();
        PointInfo subjectPointInfo = subject(givenPointInfo.getMemberNumber(), givenCashPoint());
        assertThat(subjectPointInfo.getMemberNumber()).isEqualTo(givenPointInfo.getMemberNumber());
        assertThat(subjectPointInfo.getTotalPoint()).isEqualTo(givenPointInfo.getTotalPoint() + givenCashPoint());
        assertThat(subjectPointInfo.getFreePoint()).isEqualTo(givenPointInfo.getFreePoint());
        assertThat(subjectPointInfo.getCashPoint()).isEqualTo(givenPointInfo.getCashPoint() + givenCashPoint());
        assertThat(subjectPointInfo.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }

    @Nested
    @DisplayName("무상 적립금이 차감되면")
    class Context2 {
      PointInfo subject(long memberNumber, int point) {
        return pointInfoService.minusFreePoint(memberNumber, point);
      }

      @Test
      @DisplayName("무상 적립금과 총적립금이 감소 해야 한다")
      void test() {
        PointInfo givenPointInfo = givenPointInfo();
        PointInfo subjectPointInfo = subject(givenPointInfo.getMemberNumber(), givenFreePoint());
        assertThat(subjectPointInfo.getMemberNumber()).isEqualTo(givenPointInfo.getMemberNumber());
        assertThat(subjectPointInfo.getTotalPoint()).isEqualTo(givenPointInfo.getTotalPoint() - givenFreePoint());
        assertThat(subjectPointInfo.getFreePoint()).isEqualTo(givenPointInfo.getFreePoint() - givenFreePoint());
        assertThat(subjectPointInfo.getCashPoint()).isEqualTo(givenPointInfo.getCashPoint());
        assertThat(subjectPointInfo.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }


    @Nested
    @DisplayName("유상 적립금이 차감되면")
    class Context3 {
      PointInfo subject(long memberNumber, int point) {
        return pointInfoService.minusCashPoint(memberNumber, point);
      }

      @Test
      @DisplayName("유상 적립금과 총적립금이 감소 해야 한다")
      void test() {
        PointInfo givenPointInfo = givenPointInfo();
        PointInfo subjectPointInfo = subject(givenPointInfo.getMemberNumber(), givenCashPoint());
        assertThat(subjectPointInfo.getMemberNumber()).isEqualTo(givenPointInfo.getMemberNumber());
        assertThat(subjectPointInfo.getTotalPoint()).isEqualTo(givenPointInfo.getTotalPoint() - givenCashPoint());
        assertThat(subjectPointInfo.getFreePoint()).isEqualTo(givenPointInfo.getFreePoint());
        assertThat(subjectPointInfo.getCashPoint()).isEqualTo(givenPointInfo.getCashPoint() - givenCashPoint());
        assertThat(subjectPointInfo.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }
  }

  @Nested
  @DisplayName("적립금 정보를 조회할때")
  class DescribeGetPointInfo {
    PointInfo subject() {
      return pointInfoService.getOrCratePointInfo(givenMemberNumber());
    }

    @Nested
    @DisplayName("회원의 적립금정보가 존재하지 않으면")
    class ContextWithNonExists {

      @Test
      @DisplayName("회원의 적립금 정보를 초기화 하여 생성 해야 한다")
      void test() {
        PointInfo pointInfo = subject();
        assertThat(pointInfo.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(pointInfo.getTotalPoint()).isEqualTo(0);
        assertThat(pointInfo.getFreePoint()).isEqualTo(0);
        assertThat(pointInfo.getCashPoint()).isEqualTo(0);
        assertThat(pointInfo.getUpdateTime()).isEqualToIgnoringNanos(LocalDateTime.now());
      }
    }

    @Nested
    @DisplayName("회원의 적립금정보가 존재한다면")
    class ContextWithExists {
      PointInfo givenPointInfo() {
        return pointInfoService.createPointInfo(givenMemberNumber(), 100, 200);
      }

      @Test
      @DisplayName("회원의 적립금이 조회 되어야 한다")
      void test() {
        PointInfo givenPointInfo = givenPointInfo();
        PointInfo subjectPointInfo = subject();
        assertThat(subjectPointInfo.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(subjectPointInfo.getTotalPoint()).isEqualTo(givenPointInfo.getTotalPoint());
        assertThat(subjectPointInfo.getFreePoint()).isEqualTo(givenPointInfo.getFreePoint());
        assertThat(subjectPointInfo.getCashPoint()).isEqualTo(givenPointInfo.getCashPoint());
        assertThat(subjectPointInfo.getUpdateTime()).isEqualToIgnoringNanos(givenPointInfo.getUpdateTime());
      }
    }
  }
}
