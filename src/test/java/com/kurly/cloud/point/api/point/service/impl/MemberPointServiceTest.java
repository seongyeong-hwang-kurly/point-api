package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.service.MemberPointUseCase;
import com.kurly.cloud.point.api.point.service.PublishPointUseCase;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import com.kurly.cloud.point.api.point.web.dto.MemberPointHistoryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
@DisplayName("MemberPointServiceTest class")
class MemberPointServiceTest {
  long givenMemberNumber() {
    return 1035;
  }

  @Autowired
  MemberPointUseCase memberPointAdapter;

  @Autowired
  MemberPointHistoryDomainService memberPointHistoryService;

  @Autowired
  PublishPointUseCase publishPointUseCase;

  @Nested
  @DisplayName("회원 적립금 이력을 조회 할 때")
  class DescribeGetMemberHistoryList {
    Page<MemberPointHistoryDto> subject(MemberPointHistoryListRequest request) {
      return memberPointAdapter.getMemberHistoryList(request);
    }

    @BeforeEach
    public void setUp() {
      setupHistories(givenMemberNumber());
    }

    private void setupHistories(long memberNumber) {
      insertHistoryWithHidden(memberNumber,false, 10);
      insertHistoryWithHidden(memberNumber,true, 10);
    }

    void insertHistoryWithHidden(long memberNumber, boolean hidden, int count) {
      for (int i = 0; i < count; i++) {
        memberPointHistoryService.insertHistory(
            MemberPointHistoryInsertRequest.builder()
                .freePoint(100)
                .cashPoint(10)
                .detail("설명")
                .memo("메모")
                .memberNumber(memberNumber)
                .expireTime(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()))
                .hidden(hidden)
                .orderNumber(1234)
                .regTime(LocalDateTime.now())
                .type(HistoryType.TYPE_1.getValue())
                .build()
        );
      }
    }

    @TransactionalTest
    @Nested
      @DisplayName("숨겨진 이력을 포함하도록 요청하면")
    class Context0 {
      @Test
      @DisplayName("1페이지가 조회 된다")
      void test() {
        long MEMBER_NUMBER = new Random().nextLong();
        setupHistories(MEMBER_NUMBER);

        Page<MemberPointHistoryDto> historyList = subject(givenRequest(MEMBER_NUMBER, 0));
        assertThat(historyList.getTotalElements()).isEqualTo(20);
        assertThat(historyList.getTotalPages()).isEqualTo(2);
        assertThat(historyList.getNumberOfElements()).isEqualTo(10);
      }

      MemberPointHistoryListRequest givenRequest(long memberNumber, int page) {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(memberNumber)
            .page(page)
            .includeHidden(true)
            .build();
      }

      @Test
      @DisplayName("2페이지는 조회 되고 3페이지는 조회되지 않는다")
      void test1() {
        long MEMBER_NUMBER = new Random().nextLong();
        setupHistories(MEMBER_NUMBER);

        Page<MemberPointHistoryDto> historyList = subject(givenRequest(MEMBER_NUMBER, 1));
        assertThat(historyList.getTotalElements()).isEqualTo(20);
        assertThat(historyList.getTotalPages()).isEqualTo(2);
        assertThat(historyList.getNumberOfElements()).isEqualTo(10);

        historyList = subject(givenRequest(MEMBER_NUMBER, 2));
        assertThat(historyList.getNumberOfElements()).isEqualTo(0);
      }

    }

    @TransactionalTest
    @Nested
      @DisplayName("숨겨진 이력을 제외하고 조회한다면")
    class Context1 {
      @Test
      @DisplayName("총 10개의 이력이 조회 된다")
      void test() {
        long MEMBER_NUMBER = new Random().nextLong();
        setupHistories(MEMBER_NUMBER);

        Page<MemberPointHistoryDto> historyList = subject(givenRequest(MEMBER_NUMBER));
        assertThat(historyList.getTotalElements()).isEqualTo(10);
      }

      MemberPointHistoryListRequest givenRequest(long memberNumber) {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(memberNumber)
            .includeHidden(false)
            .build();
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("memo 필드를 포함 한다면")
    class Context2 {
      @Test
      @DisplayName("memo 필드값이 존재 해야 한다")
      void test() {
        Page<MemberPointHistoryDto> historyList = subject(givenRequest());
        assertThat(historyList.getContent().get(0).getMemo()).isNotEmpty();
      }

      MemberPointHistoryListRequest givenRequest() {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .includeMemo(true)
            .build();
      }
    }

    @TransactionalTest
    @Nested
      @DisplayName("memo 필드를 포함 하지 않는다면")
    class Context3 {
      @Test
      @DisplayName("memo 필드값이 존재 하지 않아야 한다")
      void test() {
        Page<MemberPointHistoryDto> historyList = subject(givenRequest());
        assertThat(historyList.getContent().get(0).getMemo()).isNullOrEmpty();
      }

      MemberPointHistoryListRequest givenRequest() {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .build();
      }
    }
  }

  @Nested
  @DisplayName("회원 적립금 요약을 조회 할 때")
  class DescribeGetMemberPointSummary {
    MemberPointSummary subject(long memberNumber) {
      return memberPointAdapter.getMemberPointSummary(memberNumber);
    }

    long givenPointAmount() {
      return 1000;
    }

    @TransactionalTest
    @Nested
      @DisplayName("적립금이 없으면")
    class Context0 {
      @Test
      @DisplayName("만료일은 시스템 기본 만료금액은 0으로 리턴한다")
      void test() {
        long MEMBER_NUMBER = new Random().nextLong();

        MemberPointSummary memberPointSummary = subject(MEMBER_NUMBER);
        assertThat(memberPointSummary.getAmount()).isEqualTo(0);
        assertThat(memberPointSummary.getNextExpireDate())
            .isEqualTo(PointExpireDateCalculator.calculateNext(LocalDateTime.now()));
        assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(0);

      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("유상적립금이 있으면")
    class Context1 {

      @Test
      @DisplayName("만료일은 시스템 기본 만료금액은 0으로 리턴한다")
      void test() {
        long MEMBER_NUMBER = new Random().nextLong();
        givenPoint(MEMBER_NUMBER);
        MemberPointSummary memberPointSummary = subject(MEMBER_NUMBER);
        assertThat(memberPointSummary.getAmount()).isEqualTo(givenPointAmount());
        assertThat(memberPointSummary.getNextExpireDate())
            .isEqualTo(PointExpireDateCalculator.calculateNext(LocalDateTime.now()));
        assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(0);

      }

      void givenPoint(long memberNumber) {
        publishPointUseCase.publish(PublishPointRequest.builder()
            .point(givenPointAmount())
            .memberNumber(memberNumber)
            .settle(true)
            .historyType(HistoryType.TYPE_12.getValue())
            .actionMemberNumber(memberNumber)
            .detail("")
            .memo("")
            .build());
      }
    }

    @Nested
      @DisplayName("만료될 적립금이 있으면")
    class Context2 {

      @TransactionalTest
      @Nested
      @DisplayName("다음 만료일이 시스템 만료일과 같으면")
      class Context2_Context0 {

        @Test
        @DisplayName("다음 시스템 만료일에 만료예정 적립금을 리턴한다")
        void test() {
          long MEMBER_NUMBER = new Random().nextLong();
          givenPoint(MEMBER_NUMBER);
          MemberPointSummary memberPointSummary = subject(MEMBER_NUMBER);

          assertThat(memberPointSummary.getAmount()).isEqualTo(givenPointAmount());
          assertThat(memberPointSummary.getNextExpireDate())
              .isEqualTo(PointExpireDateCalculator.calculateNext(LocalDateTime.now()));
          assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(givenPointAmount());
        }

        void givenPoint(long memberNumber) {
          publishPointUseCase.publish(PublishPointRequest.builder()
              .point(givenPointAmount())
              .memberNumber(memberNumber)
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(memberNumber)
              .detail("")
              .memo("")
              .expireDate(PointExpireDateCalculator
                  .calculateNext(LocalDateTime.now()))
              .build());
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("다음 만료일이 시스템 만료일 이전이면")
      class Context2_Context1 {

        @Test
        @DisplayName("해당 적립금의 만료일과 만료 예정금액을 리턴한다")
        void test() {
          long MEMBER_NUMBER = new Random().nextLong();
          givenPoint(MEMBER_NUMBER);
          MemberPointSummary memberPointSummary = subject(MEMBER_NUMBER);

          assertThat(memberPointSummary.getAmount()).isEqualTo(givenPointAmount());
          assertThat(memberPointSummary.getNextExpireDate())
              .isEqualTo(givenExpireDate());
          assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(givenPointAmount());
        }

        void givenPoint(long memberNumber) {
          publishPointUseCase.publish(PublishPointRequest.builder()
              .point(givenPointAmount())
              .memberNumber(memberNumber)
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(memberNumber)
              .detail("")
              .memo("")
              .expireDate(givenExpireDate())
              .build());
        }

        LocalDateTime givenExpireDate() {
          return PointExpireDateCalculator
              .calculateNext(LocalDateTime.now()).minusDays(1);
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("다음 만료일이 시스템 만료일과 이후이면")
      class Context2_Context2 {
        @Test
        @DisplayName("만료일은 시스템 기본 만료금액은 0으로 리턴한다")
        void test() {
          long MEMBER_NUMBER = new Random().nextLong();

          givenPoint(MEMBER_NUMBER);
          MemberPointSummary memberPointSummary = subject(MEMBER_NUMBER);
          assertThat(memberPointSummary.getAmount()).isEqualTo(givenPointAmount());
          assertThat(memberPointSummary.getNextExpireDate())
              .isEqualTo(PointExpireDateCalculator.calculateNext(LocalDateTime.now()));
          assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(0);
        }

        void givenPoint(long memberNumber) {
          publishPointUseCase.publish(PublishPointRequest.builder()
              .point(givenPointAmount())
              .memberNumber(memberNumber)
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(memberNumber)
              .detail("")
              .memo("")
              .expireDate(givenExpireDate())
              .build());
        }

        LocalDateTime givenExpireDate() {
          return PointExpireDateCalculator.calculateDefault(LocalDateTime.now()).plusDays(1);
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("다음 만료일에 만료될 적립금이 여러건 있다면")
      class Context2_Context3 {

        @Test
        @DisplayName("만료 될 적립금의 합산을 리턴한다")
        void test() {
          long MEMBER_NUMBER = new Random().nextLong();
          givenPoint(MEMBER_NUMBER);
          givenNonExpirePoint(MEMBER_NUMBER);
          MemberPointSummary memberPointSummary = subject(MEMBER_NUMBER);

          assertThat(memberPointSummary.getAmount())
              .isEqualTo(
                  givenPointAmount() * givenCount() + givenPointAmount() + givenPointAmount());
          assertThat(memberPointSummary.getNextExpireDate())
              .isEqualTo(PointExpireDateCalculator
                  .calculateNext(LocalDateTime.now()));
          assertThat(memberPointSummary.getNextExpireAmount())
              .isEqualTo(givenPointAmount() * givenCount());
        }

        void givenPoint(long memberNumber) {
          for (int i = 0; i < givenCount(); i++) {
            publishPointUseCase.publish(PublishPointRequest.builder()
                .point(givenPointAmount())
                .memberNumber(memberNumber)
                .historyType(HistoryType.TYPE_12.getValue())
                .actionMemberNumber(memberNumber)
                .detail("")
                .memo("")
                .expireDate(PointExpireDateCalculator
                    .calculateNext(LocalDateTime.now()))
                .build());
          }
        }

        void givenNonExpirePoint(long memberNumber) {
          publishPointUseCase.publish(PublishPointRequest.builder()
              .settle(true)
              .point(givenPointAmount())
              .memberNumber(memberNumber)
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(memberNumber)
              .detail("")
              .memo("")
              .build());

          publishPointUseCase.publish(PublishPointRequest.builder()
              .point(givenPointAmount())
              .memberNumber(memberNumber)
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(memberNumber)
              .detail("")
              .memo("")
              .expireDate(
                  PointExpireDateCalculator.calculateDefault(LocalDateTime.now()).plusDays(1))
              .build());
        }

        int givenCount() {
          return 5;
        }
      }
    }
  }

  @Nested
  @DisplayName("회원 사용가능 적립금을 조회 할 때")
  class DescribeGetMemberPoint {

    MemberPoint subject(long memberNumber) {
      return memberPointAdapter.getMemberPoint(memberNumber);
    }

    long givenPointAmount() {
      return 1000;
    }

    @TransactionalTest
    @Nested
    @DisplayName("적립금이 없으면")
    class Context0 {
      @Test
      @DisplayName("사용 가능 적립금은 0으로 리턴한다")
      void test() {
        long MEMBER_NUMBER = new Random().nextLong();

        MemberPoint memberPoint = subject(MEMBER_NUMBER);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(0);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
        assertThat(memberPoint.getFreePoint()).isEqualTo(0);
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("유상적립금이 있으면")
    class Context1 {

      @Test
          @DisplayName("총 적립금과 유상적립금을 리턴한다")
      void test() {
        long MEMBER_NUMBER = new Random().nextLong();
        givenPoint(MEMBER_NUMBER);
        MemberPoint memberPoint = subject(MEMBER_NUMBER);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenPointAmount());
        assertThat(memberPoint.getCashPoint()).isEqualTo(givenPointAmount());
        assertThat(memberPoint.getFreePoint()).isEqualTo(0);

      }

      void givenPoint(long memberNumber) {
        publishPointUseCase.publish(PublishPointRequest.builder()
            .point(givenPointAmount())
            .memberNumber(memberNumber)
            .settle(true)
            .historyType(HistoryType.TYPE_12.getValue())
            .actionMemberNumber(memberNumber)
            .detail("")
            .memo("")
            .build());
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("무상적립금이 있으면")
    class Context2 {

      @Test
          @DisplayName("총 적립금과 무상적립금을 리턴한다")
      void test() {
        long MEMBER_NUMBER = new Random().nextLong();
        givenPoint(MEMBER_NUMBER);
        MemberPoint memberPoint = subject(MEMBER_NUMBER);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenPointAmount());
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
        assertThat(memberPoint.getFreePoint()).isEqualTo(givenPointAmount());
      }

      void givenPoint(long memberNumber) {
        publishPointUseCase.publish(PublishPointRequest.builder()
            .point(givenPointAmount())
            .memberNumber(memberNumber)
            .settle(false)
            .historyType(HistoryType.TYPE_12.getValue())
            .actionMemberNumber(givenMemberNumber())
            .detail("")
            .memo("")
            .build());
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("유상 무상적립금이 모두 있으면")
    class Context3 {

      @Test
      @DisplayName("총 적립금은 유상 적립금과 무상적립금의 합산을 리턴한다")
      void test() {
        long MEMBER_NUMBER = new Random().nextLong();
        givenPoint(MEMBER_NUMBER);
        MemberPoint memberPoint = subject(MEMBER_NUMBER);
        assertThat(memberPoint.getTotalPoint()).isEqualTo(givenPointAmount() + givenPointAmount());
        assertThat(memberPoint.getCashPoint()).isEqualTo(givenPointAmount());
        assertThat(memberPoint.getFreePoint()).isEqualTo(givenPointAmount());

      }

      void givenPoint(long memberNumber) {
        publishPointUseCase.publish(PublishPointRequest.builder()
            .point(givenPointAmount())
            .memberNumber(memberNumber)
            .settle(false)
            .historyType(HistoryType.TYPE_12.getValue())
            .actionMemberNumber(memberNumber)
            .detail("")
            .memo("")
            .build());

        publishPointUseCase.publish(PublishPointRequest.builder()
            .point(givenPointAmount())
            .memberNumber(memberNumber)
            .settle(true)
            .historyType(HistoryType.TYPE_12.getValue())
            .actionMemberNumber(memberNumber)
            .detail("")
            .memo("")
            .build());
      }
    }
  }
}
