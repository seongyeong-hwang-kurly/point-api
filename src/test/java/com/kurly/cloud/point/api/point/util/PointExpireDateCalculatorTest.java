package com.kurly.cloud.point.api.point.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


import java.time.LocalDateTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PointExpireDateCalculator class")
public class PointExpireDateCalculatorTest {

  LocalDateTime given(MonthDay monthDay) {
    return LocalDateTime.of(2020, monthDay.getMonth(), monthDay.getDayOfMonth(), 0, 0, 0);
  }

  @Nested
  @DisplayName("calculateDefault method")
  class DescribeCalculateDefault {

    @Nested
    @DisplayName("입력값이 임의의 날짜 일 때")
    class ContextWithDate {

      LocalDateTime 입력일 = given(MonthDay.of(Month.JANUARY, 1));

      void givenCalculator(String strategy) {
        new PointExpireDateCalculator(strategy);
      }

      @Nested
      @DisplayName("기본설정이 'QUARTER'로 설정 된다면")
      class Context0 {


        @Test
        @DisplayName("2021-3-31 23:59:59를 리턴한다")
        public void test() {
          givenCalculator("QUARTER");
          LocalDateTime expireDate = PointExpireDateCalculator.calculateDefault(입력일);
          assertThat(expireDate).isEqualToIgnoringSeconds(
              LocalDateTime.of(2021, Month.MARCH, 31, 23, 59, 59)
          );
        }
      }

      @Nested
      @DisplayName("기본설정이 'NEXT_YEAR'로 설정 된다면")
      class Context1 {

        @Test
        @DisplayName("2021-1-1 23:59:59를 리턴한다")
        public void test() {
          givenCalculator("NEXT_YEAR");
          LocalDateTime expireDate = PointExpireDateCalculator.calculateDefault(입력일);
          assertThat(expireDate).isEqualToIgnoringSeconds(
              LocalDateTime.of(2021, 입력일.getMonth(), 입력일.getDayOfMonth(), 23, 59, 59)
          );
        }
      }

      @Nested
      @DisplayName("기본설정이 알 수 없는 값 이면")
      class Context2 {

        @Test
        @DisplayName("예외가 발생 한다")
        public void test() {
          try {
            givenCalculator("SOME_CONFIG");
            PointExpireDateCalculator.calculateDefault(입력일);
            fail("실행되면 안되는 코드");
          } catch (IllegalStateException expected) {
            assertThat(expected.getMessage()).isEqualTo("존재하지 않는 적립금 만료 정책입니다");
          }
        }
      }
    }
  }

  @Nested
  @DisplayName("calculateNext method")
  class DescribeCalculateNext {

    @Nested
    @DisplayName("입력값이 임의의 날짜 일 때")
    class ContextWithDate {

      LocalDateTime 입력일 = given(MonthDay.of(Month.JANUARY, 1));

      void givenCalculator(String strategy) {
        new PointExpireDateCalculator(strategy);
      }

      @Nested
      @DisplayName("기본설정이 'QUARTER'로 설정 된다면")
      class Context0 {


        @Test
        @DisplayName("2020-3-31 23:59:59를 리턴한다")
        public void test() {
          givenCalculator("QUARTER");
          LocalDateTime expireDate = PointExpireDateCalculator.calculateNext(입력일);
          assertThat(expireDate).isEqualToIgnoringSeconds(
              LocalDateTime.of(2020, Month.MARCH, 31, 23, 59, 59)
          );
        }
      }

      @Nested
      @DisplayName("기본설정이 'NEXT_YEAR'로 설정 된다면")
      class Context1 {

        @Test
        @DisplayName("2020-1-2 23:59:59를 리턴한다")
        public void test() {
          givenCalculator("NEXT_YEAR");
          LocalDateTime expireDate = PointExpireDateCalculator.calculateNext(입력일);
          assertThat(expireDate).isEqualToIgnoringSeconds(
              LocalDateTime.of(2020, 입력일.getMonth(), 2, 23, 59, 59)
          );
        }
      }
    }
  }
}
