package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryDto;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("MemberPointAdapter class")
class MemberPointAdapterTest implements CommonTestGiven {

  @Autowired
  MemberPointAdapter memberPointAdapter;

  @Autowired
  MemberPointHistoryService memberPointHistoryService;

  @Autowired
  PublishPointPort publishPointPort;

  @Nested
  @DisplayName("회원 적립금 이력을 조회 할 때")
  class DescribeGetMemberHistoryList {
    Page<MemberPointHistoryDto> subject(MemberPointHistoryListRequest request) {
      return memberPointAdapter.getMemberHistoryList(request);
    }

    void insertHistoryWithHidden(boolean hidden, int count) {
      for (int i = 0; i < count; i++) {
        memberPointHistoryService.insertHistory(
            MemberPointHistoryInsertRequest.builder()
                .freePoint(100)
                .cashPoint(10)
                .detail("설명")
                .memo("메모")
                .memberNumber(givenMemberNumber())
                .expireTime(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()))
                .hidden(hidden)
                .orderNumber(1234)
                .regTime(LocalDateTime.now())
                .type(HistoryType.TYPE_1.getValue())
                .build()
        );
      }
    }

    @BeforeEach
    public void setUp() {
      insertHistoryWithHidden(false, 10);
      insertHistoryWithHidden(true, 10);
    }

    @TransactionalTest
    @Nested
    @DisplayName("숨겨진 이력을 포함하도록 요청하면")
    class Context0 {
      MemberPointHistoryListRequest givenRequest(int page) {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .page(page)
            .includeHidden(true)
            .build();
      }

      @Test
      @DisplayName("1페이지가 조회 된다")
      void test() {
        Page<MemberPointHistoryDto> historyList = subject(givenRequest(0));
        assertThat(historyList.getTotalElements()).isEqualTo(20);
        assertThat(historyList.getTotalPages()).isEqualTo(2);
        assertThat(historyList.getNumberOfElements()).isEqualTo(10);
      }

      @Test
      @DisplayName("2페이지는 조회 되고 3페이지는 조회되지 않는다")
      void test1() {
        Page<MemberPointHistoryDto> historyList = subject(givenRequest(1));
        assertThat(historyList.getTotalElements()).isEqualTo(20);
        assertThat(historyList.getTotalPages()).isEqualTo(2);
        assertThat(historyList.getNumberOfElements()).isEqualTo(10);

        historyList = subject(givenRequest(2));
        assertThat(historyList.getNumberOfElements()).isEqualTo(0);
      }

    }

    @TransactionalTest
    @Nested
    @DisplayName("숨겨진 이력을 제외하고 조회한다면")
    class Context1 {
      MemberPointHistoryListRequest givenRequest() {
        return MemberPointHistoryListRequest.builder()
            .memberNumber(givenMemberNumber())
            .includeHidden(false)
            .build();
      }

      @Test
      @DisplayName("총 10개의 이력이 조회 된다")
      void test() {
        Page<MemberPointHistoryDto> historyList = subject(givenRequest());
        assertThat(historyList.getTotalElements()).isEqualTo(10);
      }
    }
  }

  @Nested
  @DisplayName("회원 적립금 요약을 조회 할 때")
  class DescribeGetMemberPointSummary {
    MemberPointSummary subject() {
      return memberPointAdapter.getMemberPointSummary(givenMemberNumber());
    }

    int givenPointAmount() {
      return 1000;
    }

    @TransactionalTest
    @Nested
    @DisplayName("포인트가 없으면")
    class Context0 {
      @Test
      @DisplayName("만료일은 시스템 기본 만료금액은 0으로 리턴한다")
      void test() {
        MemberPointSummary memberPointSummary = subject();
        assertThat(memberPointSummary.getAmount()).isEqualTo(0);
        assertThat(memberPointSummary.getNextExpireDate())
            .isEqualTo(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()));
        assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(0);

      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("유상포인트가 있으면")
    class Context1 {

      void givenPoint() {
        publishPointPort.publish(PublishPointRequest.builder()
            .point(givenPointAmount())
            .memberNumber(givenMemberNumber())
            .settle(true)
            .historyType(HistoryType.TYPE_12.getValue())
            .actionMemberNumber(givenMemberNumber())
            .detail("")
            .memo("")
            .build());
      }

      @Test
      @DisplayName("만료일은 시스템 기본 만료금액은 0으로 리턴한다")
      void test() {
        givenPoint();
        MemberPointSummary memberPointSummary = subject();
        assertThat(memberPointSummary.getAmount()).isEqualTo(givenPointAmount());
        assertThat(memberPointSummary.getNextExpireDate())
            .isEqualTo(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()));
        assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(0);

      }
    }

    @Nested
    @DisplayName("만료될 포인트가 있으면")
    class Context2 {

      @TransactionalTest
      @Nested
      @DisplayName("다음 만료일이 시스템 만료일과 같으면")
      class Context2_Context0 {

        void givenPoint() {
          publishPointPort.publish(PublishPointRequest.builder()
              .point(givenPointAmount())
              .memberNumber(givenMemberNumber())
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(givenMemberNumber())
              .detail("")
              .memo("")
              .expireDate(PointExpireDateCalculator
                  .calculateDefault(LocalDateTime.now().minusYears(1)))
              .build());
        }

        @Test
        @DisplayName("다음 시스템 만료일에 만료예정 포인트를 리턴한다")
        void test() {
          givenPoint();
          MemberPointSummary memberPointSummary = subject();

          assertThat(memberPointSummary.getAmount()).isEqualTo(givenPointAmount());
          assertThat(memberPointSummary.getNextExpireDate())
              .isEqualTo(PointExpireDateCalculator
                  .calculateDefault(LocalDateTime.now().minusYears(1)));
          assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(givenPointAmount());
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("다음 만료일이 시스템 만료일 이전이면")
      class Context2_Context1 {

        LocalDateTime givenExpireDate() {
          return PointExpireDateCalculator
              .calculateDefault(LocalDateTime.now().minusYears(1)).minusDays(1);
        }

        void givenPoint() {
          publishPointPort.publish(PublishPointRequest.builder()
              .point(givenPointAmount())
              .memberNumber(givenMemberNumber())
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(givenMemberNumber())
              .detail("")
              .memo("")
              .expireDate(givenExpireDate())
              .build());
        }

        @Test
        @DisplayName("해당 포인트의 만료일과 만료 예정금액을 리턴한다")
        void test() {
          givenPoint();
          MemberPointSummary memberPointSummary = subject();

          assertThat(memberPointSummary.getAmount()).isEqualTo(givenPointAmount());
          assertThat(memberPointSummary.getNextExpireDate())
              .isEqualTo(givenExpireDate());
          assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(givenPointAmount());
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("다음 만료일이 시스템 만료일과 이후이면")
      class Context2_Context2 {
        LocalDateTime givenExpireDate() {
          return PointExpireDateCalculator.calculateDefault(LocalDateTime.now()).plusDays(1);
        }

        void givenPoint() {
          publishPointPort.publish(PublishPointRequest.builder()
              .point(givenPointAmount())
              .memberNumber(givenMemberNumber())
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(givenMemberNumber())
              .detail("")
              .memo("")
              .expireDate(givenExpireDate())
              .build());
        }

        @Test
        @DisplayName("만료일은 시스템 기본 만료금액은 0으로 리턴한다")
        void test() {
          givenPoint();
          MemberPointSummary memberPointSummary = subject();
          assertThat(memberPointSummary.getAmount()).isEqualTo(givenPointAmount());
          assertThat(memberPointSummary.getNextExpireDate())
              .isEqualTo(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()));
          assertThat(memberPointSummary.getNextExpireAmount()).isEqualTo(0);
        }
      }

      @TransactionalTest
      @Nested
      @DisplayName("다음 만료일에 만료될 포인트가 여러건 있다면")
      class Context2_Context3 {

        int givenCount() {
          return 5;
        }

        void givenPoint() {
          for (int i = 0; i < givenCount(); i++) {
            publishPointPort.publish(PublishPointRequest.builder()
                .point(givenPointAmount())
                .memberNumber(givenMemberNumber())
                .historyType(HistoryType.TYPE_12.getValue())
                .actionMemberNumber(givenMemberNumber())
                .detail("")
                .memo("")
                .expireDate(PointExpireDateCalculator
                    .calculateDefault(LocalDateTime.now().minusYears(1)))
                .build());
          }
        }

        void givenNonExpirePoint() {
          publishPointPort.publish(PublishPointRequest.builder()
              .settle(true)
              .point(givenPointAmount())
              .memberNumber(givenMemberNumber())
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(givenMemberNumber())
              .detail("")
              .memo("")
              .build());

          publishPointPort.publish(PublishPointRequest.builder()
              .point(givenPointAmount())
              .memberNumber(givenMemberNumber())
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(givenMemberNumber())
              .detail("")
              .memo("")
              .expireDate(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()).plusDays(1))
              .build());
        }

        @Test
        @DisplayName("만료 될 포인트의 합산을 리턴한다")
        void test() {
          givenPoint();
          givenNonExpirePoint();
          MemberPointSummary memberPointSummary = subject();

          assertThat(memberPointSummary.getAmount())
              .isEqualTo(givenPointAmount() * givenCount() + givenPointAmount() + givenPointAmount());
          assertThat(memberPointSummary.getNextExpireDate())
              .isEqualTo(PointExpireDateCalculator
                  .calculateDefault(LocalDateTime.now().minusYears(1)));
          assertThat(memberPointSummary.getNextExpireAmount())
              .isEqualTo(givenPointAmount() * givenCount());
        }
      }
    }
  }
}
