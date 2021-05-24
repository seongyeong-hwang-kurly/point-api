package com.kurly.cloud.point.api.point.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
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

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("MemberPointDomainServiceTest class")
class MemberPointDomainServiceTest implements CommonTestGiven {
  @Autowired
  MemberPointDomainService memberPointDomainService;

  @Autowired
  MemberPointRepository memberPointRepository;

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
      @Test
      @DisplayName("생성 된다")
      void test() {
        MemberPoint subject = subject();
        assertThat(subject.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(subject.getTotalPoint()).isEqualTo(givenTotalPoint());
        assertThat(subject.getFreePoint()).isEqualTo(givenFreePoint());
        assertThat(subject.getCashPoint()).isEqualTo(givenCashPoint());
        assertThat(subject.getUpdateTime()).isEqualToIgnoringSeconds(LocalDateTime.now());
      }

      MemberPoint subject() {
        return memberPointDomainService
            .createMemberPoint(givenMemberNumber(), givenFreePoint(), givenCashPoint());
      }

      int givenTotalPoint() {
        return givenFreePoint() + givenCashPoint();
      }

      int givenFreePoint() {
        return 100;
      }

      int givenCashPoint() {
        return 200;
      }
    }
  }

  @Nested
  @DisplayName("적립금이 추가 또는 차감 될때")
  class DescribePlusMinusPoint {

    MemberPoint givenMemberPoint() {
      return memberPointDomainService
          .createMemberPoint(givenMemberNumber(), givenFreePoint(), givenCashPoint());
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
      @Test
      @DisplayName("무상 적립금과 총적립금이 증가 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint =
            subject(givenMemberPoint.getMemberNumber(), givenFreePoint());
        assertThat(subjectMemberPoint.getMemberNumber())
            .isEqualTo(givenMemberPoint.getMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint())
            .isEqualTo(givenMemberPoint.getTotalPoint() + givenFreePoint());
        assertThat(subjectMemberPoint.getFreePoint())
            .isEqualTo(givenMemberPoint.getFreePoint() + givenFreePoint());
        assertThat(subjectMemberPoint.getCashPoint()).isEqualTo(givenMemberPoint.getCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime())
            .isEqualToIgnoringSeconds(LocalDateTime.now());
      }

      MemberPoint subject(long memberNumber, int point) {
        return memberPointDomainService.plusFreePoint(memberNumber, point);
      }
    }

    @Nested
    @DisplayName("유상 적립금이 추가되면")
    class Context1 {
      @Test
      @DisplayName("유상 적립금과 총적립금이 증가 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint =
            subject(givenMemberPoint.getMemberNumber(), givenCashPoint());
        assertThat(subjectMemberPoint.getMemberNumber())
            .isEqualTo(givenMemberPoint.getMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint())
            .isEqualTo(givenMemberPoint.getTotalPoint() + givenCashPoint());
        assertThat(subjectMemberPoint.getFreePoint()).isEqualTo(givenMemberPoint.getFreePoint());
        assertThat(subjectMemberPoint.getCashPoint())
            .isEqualTo(givenMemberPoint.getCashPoint() + givenCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime())
            .isEqualToIgnoringSeconds(LocalDateTime.now());
      }

      MemberPoint subject(long memberNumber, int point) {
        return memberPointDomainService.plusCashPoint(memberNumber, point);
      }
    }

    @Nested
    @DisplayName("무상 적립금이 차감되면")
    class Context2 {
      @Test
      @DisplayName("무상 적립금과 총적립금이 감소 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint =
            subject(givenMemberPoint.getMemberNumber(), givenFreePoint());
        assertThat(subjectMemberPoint.getMemberNumber())
            .isEqualTo(givenMemberPoint.getMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint())
            .isEqualTo(givenMemberPoint.getTotalPoint() - givenFreePoint());
        assertThat(subjectMemberPoint.getFreePoint())
            .isEqualTo(givenMemberPoint.getFreePoint() - givenFreePoint());
        assertThat(subjectMemberPoint.getCashPoint()).isEqualTo(givenMemberPoint.getCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime())
            .isEqualToIgnoringSeconds(LocalDateTime.now());
      }

      MemberPoint subject(long memberNumber, int point) {
        return memberPointDomainService.minusFreePoint(memberNumber, point);
      }
    }


    @Nested
    @DisplayName("유상 적립금이 차감되면")
    class Context3 {
      @Test
      @DisplayName("유상 적립금과 총적립금이 감소 한다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint =
            subject(givenMemberPoint.getMemberNumber(), givenCashPoint());
        assertThat(subjectMemberPoint.getMemberNumber())
            .isEqualTo(givenMemberPoint.getMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint())
            .isEqualTo(givenMemberPoint.getTotalPoint() - givenCashPoint());
        assertThat(subjectMemberPoint.getFreePoint()).isEqualTo(givenMemberPoint.getFreePoint());
        assertThat(subjectMemberPoint.getCashPoint())
            .isEqualTo(givenMemberPoint.getCashPoint() - givenCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime())
            .isEqualToIgnoringSeconds(LocalDateTime.now());
      }

      MemberPoint subject(long memberNumber, int point) {
        return memberPointDomainService.minusCashPoint(memberNumber, point);
      }
    }
  }

  @Nested
  @DisplayName("회원에게 대출한 적립금이 잇을 때")
  class DescribeHasDebtPoint {
    MemberPoint givenMemberPoint() {
      return memberPointDomainService
          .createMemberPoint(givenMemberNumber(), givenFreePoint(), givenCashPoint());
    }

    int givenFreePoint() {
      return -1000;
    }

    int givenCashPoint() {
      return 0;
    }

    @TransactionalTest
    @Nested
    @DisplayName("대출한 적립금만큼 무상 적립금이 추가되면")
    class Context0 {
      @DisplayName("회원의 총 적립금은 0원이 된다")
      @Test
      void test() {
        MemberPoint memberPoint = givenMemberPoint();
        subject(given());
        assertThat(memberPoint.getTotalPoint()).isEqualTo(0);
        assertThat(memberPoint.getFreePoint()).isEqualTo(0);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }

      MemberPoint subject(int amount) {
        return memberPointDomainService.plusFreePoint(givenMemberNumber(), amount);
      }

      int given() {
        return 1000;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("대출한 적립금 보다 적은 무상 적립금이 추가되면")
    class Context1 {
      @DisplayName("회원의 총 적립금은 0보다 작고 (대출적립금 - 추가적립금)가 된다")
      @Test
      void test() {
        MemberPoint memberPoint = givenMemberPoint();
        subject(given());
        assertThat(memberPoint.getTotalPoint()).isLessThan(0);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenFreePoint() + given());
        assertThat(memberPoint.getFreePoint()).isEqualTo(givenFreePoint() + given());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }

      MemberPoint subject(int amount) {
        return memberPointDomainService.plusFreePoint(givenMemberNumber(), amount);
      }

      int given() {
        return 500;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("대출한 적립금 보다 많은 무상 적립금이 추가되면")
    class Context2 {
      @DisplayName("회원의 총 적립금은 0 보다 크고 (추가적립금 - 대출적립금)가 된다")
      @Test
      void test() {
        MemberPoint memberPoint = givenMemberPoint();
        subject(given());
        assertThat(memberPoint.getTotalPoint()).isGreaterThan(0);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenFreePoint() + given());
        assertThat(memberPoint.getFreePoint()).isEqualTo(givenFreePoint() + given());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }

      MemberPoint subject(int amount) {
        return memberPointDomainService.plusFreePoint(givenMemberNumber(), amount);
      }

      int given() {
        return 3000;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("대출한 적립금만큼 유상 적립금이 추가되면")
    class Context3 {
      @DisplayName("회원의 총 적립금은 0원이 된다")
      @Test
      void test() {
        MemberPoint memberPoint = givenMemberPoint();
        subject(given());
        assertThat(memberPoint.getTotalPoint()).isEqualTo(0);
        assertThat(memberPoint.getFreePoint()).isEqualTo(0);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }

      MemberPoint subject(int amount) {
        return memberPointDomainService.plusCashPoint(givenMemberNumber(), amount);
      }

      int given() {
        return 1000;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("대출한 적립금 보다 적은 적립금이 추가되면")
    class Context4 {
      @DisplayName("회원의 총 적립금은 0보다 작고 (대출적립금 - 추가적립금)가 된다")
      @Test
      void test() {
        MemberPoint memberPoint = givenMemberPoint();
        subject(given());
        assertThat(memberPoint.getTotalPoint()).isLessThan(0);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenFreePoint() + given());
        assertThat(memberPoint.getFreePoint()).isEqualTo(givenFreePoint() + given());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }

      MemberPoint subject(int amount) {
        return memberPointDomainService.plusCashPoint(givenMemberNumber(), amount);
      }

      int given() {
        return 500;
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("대출한 적립금 보다 많은 유상 적립금이 추가되면")
    class Context5 {
      @DisplayName("회원의 총 적립금은 0 보다 크고 (추가적립금 - 대출적립금)가 된다")
      @Test
      void test() {
        MemberPoint memberPoint = givenMemberPoint();
        subject(given());
        assertThat(memberPoint.getTotalPoint()).isGreaterThan(0);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenFreePoint() + given());
        assertThat(memberPoint.getFreePoint()).isEqualTo(0);
        assertThat(memberPoint.getCashPoint()).isEqualTo(givenFreePoint() + given());
      }

      MemberPoint subject(int amount) {
        return memberPointDomainService.plusCashPoint(givenMemberNumber(), amount);
      }

      int given() {
        return 3000;
      }
    }
  }

  @Nested
  @DisplayName("적립금 정보를 조회할때")
  class DescribeGetMemberPoint {
    MemberPoint subject() {
      return memberPointDomainService.getOrCreateMemberPoint(givenMemberNumber());
    }

    @Nested
    @DisplayName("회원의 적립금정보가 존재하지 않으면")
    class ContextWithNonExists {

      @Test
      @DisplayName("회원의 적립금 정보를 초기화 하여 생성 한다")
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
      @Test
      @DisplayName("회원의 적립금이 조회 된다")
      void test() {
        MemberPoint givenMemberPoint = givenMemberPoint();
        MemberPoint subjectMemberPoint = subject();
        assertThat(subjectMemberPoint.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(subjectMemberPoint.getTotalPoint()).isEqualTo(givenMemberPoint.getTotalPoint());
        assertThat(subjectMemberPoint.getFreePoint()).isEqualTo(givenMemberPoint.getFreePoint());
        assertThat(subjectMemberPoint.getCashPoint()).isEqualTo(givenMemberPoint.getCashPoint());
        assertThat(subjectMemberPoint.getUpdateTime())
            .isEqualToIgnoringSeconds(givenMemberPoint.getUpdateTime());
      }

      MemberPoint givenMemberPoint() {
        return memberPointDomainService.createMemberPoint(givenMemberNumber(), 100, 200);
      }
    }
  }
}
