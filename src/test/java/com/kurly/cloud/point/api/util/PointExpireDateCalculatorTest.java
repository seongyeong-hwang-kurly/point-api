/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.util;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DisplayName("PointExpireDateCalculator class")
public class PointExpireDateCalculatorTest {

  PointExpireDateCalculator givenCalculator() {
    return new PointExpireDateCalculator();
  }

  PointExpireDateCalculator givenCalculator(String defaultConfig) {
    return new PointExpireDateCalculator(defaultConfig);
  }

  LocalDateTime given(MonthDay monthDay) {
    return LocalDateTime.of(2020, monthDay.getMonth(), monthDay.getDayOfMonth(), 0, 0, 0);
  }

  @Nested
  @DisplayName("calculateNextYearQuarter method")
  class DescribeCalculateNextYearQuarter {

    LocalDateTime subject(LocalDateTime 입력일) {
      return givenCalculator().calculateNextYearQuarter(입력일);
    }

    @Nested
    @DisplayName("입력일이 1분기 일 때")
    class Context0 {
      Collection<LocalDateTime> given입력일() {
        return List.of(
            given(MonthDay.of(Month.JANUARY, 1)),
            given(MonthDay.of(Month.FEBRUARY, 1)),
            given(MonthDay.of(Month.MARCH, 31)));
      }

      @Test
      @DisplayName("다음해 1분기 마지막 일을 리턴해야 한다.")
      public void test() {
        final LocalDateTime 내년_분기_말일 = LocalDateTime.of(2021, 3, 31, 0, 0, 0);

        for (LocalDateTime 입력일 : given입력일()) {
          assertThat(subject(입력일)).isEqualTo(내년_분기_말일);
        }
      }
    }

    @Nested
    @DisplayName("입력일이 2분기 일 때")
    class Context1 {
      Collection<LocalDateTime> given입력일() {
        return List.of(
            given(MonthDay.of(Month.APRIL, 1)),
            given(MonthDay.of(Month.MAY, 31)),
            given(MonthDay.of(Month.JUNE, 12)));
      }

      @Test
      @DisplayName("다음해 2분기 마지막 일을 리턴해야 한다.")
      public void test() {
        final LocalDateTime 내년_분기_말일 = LocalDateTime.of(2021, 6, 30, 0, 0, 0);

        for (LocalDateTime 입력일 : given입력일()) {
          assertThat(subject(입력일)).isEqualTo(내년_분기_말일);
        }
      }
    }

    @Nested
    @DisplayName("입력일이 3분기 일 때")
    class Context2 {
      Collection<LocalDateTime> given입력일() {
        return List.of(
            given(MonthDay.of(Month.JULY, 1)),
            given(MonthDay.of(Month.AUGUST, 15)),
            given(MonthDay.of(Month.SEPTEMBER, 30)));
      }

      @Test
      @DisplayName("다음해 3분기 마지막 일을 리턴해야 한다.")
      public void test() {
        final LocalDateTime 내년_분기_말일 = LocalDateTime.of(2021, 9, 30, 0, 0, 0);

        for (LocalDateTime 입력일 : given입력일()) {
          assertThat(subject(입력일)).isEqualTo(내년_분기_말일);
        }
      }
    }

    @Nested
    @DisplayName("입력일이 4분기 일 때")
    class Context3 {
      Collection<LocalDateTime> given입력일() {
        return List.of(
            given(MonthDay.of(Month.OCTOBER, 31)),
            given(MonthDay.of(Month.NOVEMBER, 15)),
            given(MonthDay.of(Month.DECEMBER, 31)));
      }

      @Test
      @DisplayName("다음해 4분기 마지막 일을 리턴해야 한다.")
      public void test() {
        final LocalDateTime 내년_분기_말일 = LocalDateTime.of(2021, 12, 31, 0, 0, 0);

        for (LocalDateTime 입력일 : given입력일()) {
          assertThat(subject(입력일)).isEqualTo(내년_분기_말일);
        }
      }
    }
  }

  @Nested
  @DisplayName("calculateNextYear method")
  class DescribeCalculateNextYear {

    LocalDateTime subject(LocalDateTime 입력일) {
      return givenCalculator().calculateNextYear(입력일);
    }

    @Nested
    @DisplayName("입력일과 같은일이 다음 해에 존재한다면")
    class Context0 {

      LocalDateTime 입력일 = given(MonthDay.of(Month.JANUARY, 1));

      @Test
      @DisplayName("다음해 같은 일을 리턴해야 한다.")
      void test() {
        assertThat(subject(입력일)).isEqualTo(입력일.plusYears(1));
      }
    }

    @Nested
    @DisplayName("입력일이 윤년의 2월 29일 이라면")
    class Context1 {

      LocalDateTime 입력일 = given(MonthDay.of(Month.FEBRUARY, 29));

      @Test
      @DisplayName("다음 해 2월 28일을 리턴해야 한다.")
      void test() {
        assertThat(subject(입력일)).isEqualTo(입력일.plusYears(1));
      }
    }
  }

  @Nested
  @DisplayName("calculateDaysAfter method")
  class DescribeCalculateDaysAfter {
    @Nested
    @DisplayName("N일 후일 때")
    class Context0 {

      LocalDateTime 입력일 = given(MonthDay.of(Month.JANUARY, 1));

      @Test
      @DisplayName("N+1일 후를 리턴해야 한다.")
      public void test() {
        PointExpireDateCalculator calculator = givenCalculator();
        LocalDateTime expireDate = calculator.calculateDaysAfter(입력일, 30);
        assertThat(expireDate).isEqualTo(입력일.plusDays(30));
      }
    }
  }

  @Nested
  @DisplayName("calculateDefault method")
  class DescribeCalculateDefault {

    @Nested
    @DisplayName("입력값이 임의의 날짜 일 때")
    class ContextWithDate {

      LocalDateTime 입력일 = given(MonthDay.of(Month.JANUARY, 1));

      @Nested
      @DisplayName("기본설정이 'QUARTER'로 설정 된다면")
      class Context0 {

        PointExpireDateCalculator calculator = givenCalculator("QUARTER");

        @Test
        @DisplayName("calculateNextYearQuarter 와 같은 값을 리턴해야 한다.")
        public void test() {
          LocalDateTime expireDate = calculator.calculateDefault(입력일);
          assertThat(expireDate).isEqualTo(calculator.calculateNextYearQuarter(입력일));
        }
      }

      @Nested
      @DisplayName("기본설정이 'NEXT_YEAR'로 설정 된다면")
      class Context1 {

        PointExpireDateCalculator calculator = givenCalculator("NEXT_YEAR");

        @Test
        @DisplayName("calculateNextYear 와 같은 값을 리턴해야 한다.")
        public void test() {
          LocalDateTime expireDate = calculator.calculateDefault(입력일);
          assertThat(expireDate).isEqualTo(calculator.calculateNextYear(입력일));
        }
      }

      @Nested
      @DisplayName("기본설정이 알 수 없는 값 이면")
      class Context2 {

        PointExpireDateCalculator calculator = givenCalculator("SOME_CONFIG");

        @Test
        @DisplayName("예외가 발생해야 한다.")
        public void test() {
          try {
            calculator.calculateDefault(입력일);
            fail("실행되면 안되는 코드");
          } catch (IllegalStateException expected) {
            assertThat(expected.getMessage()).isEqualTo("존재하지 않는 적립금 만료 정책입니다");
          }
        }
      }
    }
  }
}
