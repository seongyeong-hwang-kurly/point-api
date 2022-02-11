package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.repository.MemberPointHistoryRepository;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("MemberPointHistoryDomainServiceTest class")
class MemberPointHistoryDomainServiceTest implements CommonTestGiven {

  @Autowired
  MemberPointHistoryDomainService memberPointHistoryDomainService;

  @Autowired
  MemberPointHistoryRepository memberPointHistoryRepository;

  @BeforeEach
  void init() {
    memberPointHistoryRepository.deleteAll(memberPointHistoryRepository.findAll());
  }

  @Nested
  @DisplayName("적립금 이력을 입력 할 때")
  class DescribeAddHistory {

    @Nested
    @DisplayName("MemberNumber가 없으면")
    class Context0 {
      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        MemberPointHistoryInsertRequest given = givenExceptMemberNumber();
        try {
          subject(given);
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }

      MemberPointHistoryInsertRequest givenExceptMemberNumber() {
        return MemberPointHistoryInsertRequest.builder()
            .type(HistoryType.TYPE_1.getValue())
            .build();
      }

      MemberPointHistory subject(MemberPointHistoryInsertRequest memberPointHistoryInsertRequest) {
        return memberPointHistoryDomainService.insertHistory(memberPointHistoryInsertRequest);
      }
    }

    @Nested
    @DisplayName("HistoryType이 없으면")
    class Context1 {
      @DisplayName("ConstraintViolationException 예외가 발생 한다")
      @Test
      void test() {
        MemberPointHistoryInsertRequest given = givenExceptHistoryType();
        try {
          subject(given);
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }

      MemberPointHistoryInsertRequest givenExceptHistoryType() {
        return MemberPointHistoryInsertRequest.builder()
            .memberNumber(givenMemberNumber())
            .build();
      }

      MemberPointHistory subject(MemberPointHistoryInsertRequest memberPointHistoryInsertRequest) {
        return memberPointHistoryDomainService.insertHistory(memberPointHistoryInsertRequest);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("올바른 값이 입력 된다면")
    class Context2 {
      @DisplayName("입력하고 값을 리턴 한다")
      @Test
      void test() {
        MemberPointHistoryInsertRequest given = givenRequest();
        MemberPointHistory subject = subject(given);

        assertThat(subject.getSeq()).isNotZero();
        assertThat(subject.getOrderNumber()).isEqualTo(given.getOrderNumber());
        assertThat(subject.getTotalPoint()).isEqualTo(given.getTotalPoint());
        assertThat(subject.getFreePoint()).isEqualTo(given.getFreePoint());
        assertThat(subject.getCashPoint()).isEqualTo(given.getCashPoint());
        assertThat(subject.getHistoryType()).isEqualTo(given.getType());
        assertThat(subject.getDetail()).isEqualTo(given.getDetail());
        assertThat(subject.getMemo()).isEqualTo(given.getMemo());
        assertThat(subject.isHidden()).isEqualTo(given.isHidden());
        assertThat(subject.getRegTime()).isEqualTo(given.getRegTime());
        assertThat(subject.getExpireTime()).isEqualTo(given.getExpireTime());

      }

      MemberPointHistoryInsertRequest givenRequest() {
        return MemberPointHistoryInsertRequest.builder()
            .freePoint(100)
            .cashPoint(10)
            .detail("설명")
            .memo("메모")
            .memberNumber(givenMemberNumber())
            .expireTime(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()))
            .hidden(false)
            .orderNumber(1234)
            .regTime(LocalDateTime.now())
            .type(HistoryType.TYPE_1.getValue())
            .build();
      }

      MemberPointHistory subject(MemberPointHistoryInsertRequest memberPointHistoryInsertRequest) {
        return memberPointHistoryDomainService.insertHistory(memberPointHistoryInsertRequest);
      }
    }

  }

  @Nested
  @DisplayName("적립금 이력을 조회 할 때")
  class DescribeGetHistoryList {

    Page<MemberPointHistory> getMemberPointHistory(MemberPointHistoryListRequest request) {
      return memberPointHistoryDomainService.getHistoryList(request);
    }

    @BeforeEach
    public void setUp() {
      insertHistoryWithHidden(false, 10);
      insertHistoryWithHidden(true, 10);
    }

    void insertHistoryWithHidden(boolean hidden, int count) {
      for (int i = 0; i < count; i++) {
        memberPointHistoryDomainService.insertHistory(
            MemberPointHistoryInsertRequest.builder()
                .freePoint(100)
                .cashPoint(10)
                .detail("설명")
                .memo("메모")
                .memberNumber(givenMemberNumber())
                .expireTime(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()))
                .hidden(hidden)
                .orderNumber(1234)
                .regTime(LocalDateTime.now().plusDays(i))
                .type(HistoryType.TYPE_1.getValue())
                .build()
        );
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("숨겨진 이력을 모두 조회한다면")
    class Context0 {
      @Test
      @DisplayName("총 20개의 이력이 조회 된다")
      void test() {
        Page<MemberPointHistory> historyList = getMemberPointHistory(givenRequest());
        assertThat(historyList.getTotalElements()).isEqualTo(20);
      }

      MemberPointHistoryListRequest givenRequest() {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .includeHidden(true)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("숨겨진 이력을 제외하고 조회한다면")
    class Context1 {
      @Test
      @DisplayName("총 10 이력이 조회 된다")
      void test() {
        Page<MemberPointHistory> historyList = getMemberPointHistory(givenRequest());
        assertThat(historyList.getTotalElements()).isEqualTo(10);
      }

      MemberPointHistoryListRequest givenRequest() {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .includeHidden(false)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("regDateTimeFrom 값이 있다면")
    class Context2 {
      @Test
      @DisplayName("입력값 이후의 등록일을 가진 이력만 조회 된다")
      void test() {
        Page<MemberPointHistory> historyList = getMemberPointHistory(givenRequest());
        assertThat(historyList.getTotalElements()).isEqualTo(9);
      }

      MemberPointHistoryListRequest givenRequest() {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .regDateTimeFrom(ZonedDateTime.now().plusDays(1).minusMinutes(1))
            .includeHidden(false)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("regDateTimeTo 값이 있다면")
    class Context3 {
      @Test
      @DisplayName("입력값 이전의 등록일을 가진 이력만 조회 된다")
      void test() {
        Page<MemberPointHistory> historyList = getMemberPointHistory(givenRequest());
        assertThat(historyList.getTotalElements()).isEqualTo(2);
      }

      MemberPointHistoryListRequest givenRequest() {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .regDateTimeTo(ZonedDateTime.now().plusDays(2).minusMinutes(1))
            .includeHidden(false)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("regDateTimeFrom, regDateTimeTo 값이 있다면")
    class Context4 {
      @Test
      @DisplayName("입력값 사이의 등록일을 가진 이력만 조회 된다")
      void test() {
        Page<MemberPointHistory> historyList = getMemberPointHistory(givenRequest());
        assertThat(historyList.getTotalElements()).isEqualTo(1);
      }

      MemberPointHistoryListRequest givenRequest() {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .regDateTimeFrom(ZonedDateTime.now().plusDays(1).minusMinutes(1))
            .regDateTimeTo(ZonedDateTime.now().plusDays(2).minusMinutes(1))
            .includeHidden(false)
            .build();
      }
    }
  }
}
