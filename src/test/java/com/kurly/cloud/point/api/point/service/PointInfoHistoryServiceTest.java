/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kurly.cloud.point.api.point.domain.HistoryType;
import com.kurly.cloud.point.api.point.domain.PointInfoHistoryDto;
import com.kurly.cloud.point.api.point.domain.PointInfoHistoryInsertRequestDto;
import com.kurly.cloud.point.api.point.domain.PointInfoHistoryListRequestDto;
import com.kurly.cloud.point.api.point.repository.PointInfoHistoryRepository;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import java.time.LocalDateTime;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("PointInfoHistoryService class")
class PointInfoHistoryServiceTest {

  @Autowired
  PointInfoHistoryService pointInfoHistoryService;

  @Autowired
  PointInfoHistoryRepository pointInfoHistoryRepository;

  long givenMemberNumber() {
    return 999999999;
  }

  @Nested
  @DisplayName("적립금 이력을 입력 할 때")
  class DescribeAddHistory {

    @Nested
    @DisplayName("MemberNumber가 없으면")
    class Context0 {
      PointInfoHistoryInsertRequestDto givenExceptMemberNumber() {
        return PointInfoHistoryInsertRequestDto.builder()
            .type(HistoryType.TYPE_1.getValue())
            .build();
      }

      PointInfoHistoryDto subject(PointInfoHistoryInsertRequestDto pointInfoHistoryInsertRequestDto) {
        return pointInfoHistoryService.insertHistory(pointInfoHistoryInsertRequestDto);
      }

      @DisplayName("ConstraintViolationException 예외가 발생해야 한다")
      @Test
      void test() {
        PointInfoHistoryInsertRequestDto given = givenExceptMemberNumber();
        try {
          subject(given);
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }
    }

    @Nested
    @DisplayName("HistoryType이 없으면")
    class Context1 {
      PointInfoHistoryInsertRequestDto givenExceptHistoryType() {
        return PointInfoHistoryInsertRequestDto.builder()
            .memberNumber(givenMemberNumber())
            .build();
      }

      PointInfoHistoryDto subject(PointInfoHistoryInsertRequestDto pointInfoHistoryInsertRequestDto) {
        return pointInfoHistoryService.insertHistory(pointInfoHistoryInsertRequestDto);
      }

      @DisplayName("ConstraintViolationException 예외가 발생해야 한다")
      @Test
      void test() {
        PointInfoHistoryInsertRequestDto given = givenExceptHistoryType();
        try {
          subject(given);
          fail("실행되면 안되는 코드");
        } catch (ConstraintViolationException expected) {

        }
      }
    }

    @Nested
    @SpringBootTest
    @Transactional
    @DisplayName("올바른 값이 입력 된다면")
    class Context2 {
      PointInfoHistoryInsertRequestDto givenRequest() {
        return PointInfoHistoryInsertRequestDto.builder()
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

      PointInfoHistoryDto subject(PointInfoHistoryInsertRequestDto pointInfoHistoryInsertRequestDto) {
        return pointInfoHistoryService.insertHistory(pointInfoHistoryInsertRequestDto);
      }

      @DisplayName("입력하고 값을 리턴해야 한다")
      @Test
      void test() {
        PointInfoHistoryInsertRequestDto given = givenRequest();
        PointInfoHistoryDto subject = subject(given);

        assertThat(subject.getSeq()).isNotZero();
        assertThat(subject.getOrderNumber()).isEqualTo(given.getOrderNumber());
        assertThat(subject.getPoint()).isEqualTo(given.getTotalPoint());
        assertThat(subject.getDetail()).isEqualTo(given.getDetail());
        assertThat(subject.getRegDateTime()).isEqualTo(given.getRegTime());
        assertThat(subject.getExpireDateTime()).isEqualTo(given.getExpireTime());

      }
    }

  }

  @Nested
  @DisplayName("적립금 이력을 조회 할 때")
  class DescribeGetHistoryList {

    Page<PointInfoHistoryDto> subject(PointInfoHistoryListRequestDto request) {
      return pointInfoHistoryService.getHistoryList(request);
    }

    void insertHistoryWithHidden(boolean hidden, int count) {
      for (int i = 0; i < count ; i++) {
        pointInfoHistoryService.insertHistory(
            PointInfoHistoryInsertRequestDto.builder()
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
      insertHistoryWithHidden(false,10);
      insertHistoryWithHidden(true,10);
    }

    @Transactional
    @SpringBootTest
    @Nested
    @DisplayName("숨겨진 이력을 모두 조회한다면")
    class Context0 {
      PointInfoHistoryListRequestDto givenRequest() {
        return PointInfoHistoryListRequestDto.builder()
            .memberNumber(givenMemberNumber())
            .includeHidden(true)
            .build();
      }

      @Test
      @DisplayName("총 20개의 이력이 조회 되어야 한다")
      void test() {
        Page<PointInfoHistoryDto> historyList = subject(givenRequest());
        assertThat(historyList.getTotalElements()).isEqualTo(20);
      }
    }


    @Transactional
    @SpringBootTest
    @Nested
    @DisplayName("숨겨진 이력을 제외하고 조회한다면")
    class Context1 {
      PointInfoHistoryListRequestDto givenRequest() {
        return PointInfoHistoryListRequestDto.builder()
            .memberNumber(givenMemberNumber())
            .includeHidden(false)
            .build();
      }

      @Test
      @DisplayName("총 10개의 이력이 조회 되어야 한다")
      void test() {
        Page<PointInfoHistoryDto> historyList = subject(givenRequest());
        assertThat(historyList.getTotalElements()).isEqualTo(10);
      }
    }
  }
}
