/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
public class PointExpireDateCalculator {

  private static String DEFAULT_STRATEGY = "QUARTER";

  static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("Q");

  @Value("${pointExpireDefaultStrategy:QUARTER}")
  public void setStrategy(String strategy) {
    DEFAULT_STRATEGY = strategy;
  }

  public PointExpireDateCalculator(String strategy) {
    DEFAULT_STRATEGY = strategy;
  }

  /**
   * N일 이후 만료 정책을 기반으로 만료일을 계산한다.
   *
   * @param from   시작 기준일
   * @param offset N일
   * @return 만료일
   */
  public static LocalDateTime calculateDaysAfter(LocalDateTime from, int offset) {
    return withEndOfDate(from.plusDays(offset));
  }

  /**
   * 해당 일자의 시각을 23:59:59로 세팅한다
   *
   * @return 23:59:59
   */
  public static LocalDateTime withEndOfDate(LocalDateTime from) {
    return from.withHour(23).withMinute(59).withSecond(59);
  }

  /**
   * 시스템 기본 정책을 기반으로 만료일을 계산한다.
   *
   * @param from 시작 기준 일
   * @return 만료 일
   */
  public static LocalDateTime calculateDefault(LocalDateTime from) {
    switch (DEFAULT_STRATEGY) {
      case "QUARTER":
        return calculateNextYearQuarter(from);
      case "NEXT_YEAR":
        return calculateNextYear(from);
      default:
        throw new IllegalStateException("존재하지 않는 적립금 만료 정책입니다");
    }
  }

  /**
   * 내년 분기 말일 만료 정책을 기반으로 만료일을 계산한다.
   *
   * @param from 시작 기준 일
   * @return 만료일
   */
  public static LocalDateTime calculateNextYearQuarter(LocalDateTime from) {
    int quarter = Integer.parseInt(from.format(dateTimeFormatter));
    int quarterMonth = quarter * 3; // 분기에 3을 곱하면 분기 말월
    LocalDateTime quarterMonthLastDay = LocalDateTime.of(from.getYear(), quarterMonth, 1, 0, 0);
    //분기 말월의 다음달 1일에서 -1일을 하면 분기 말일
    quarterMonthLastDay = quarterMonthLastDay.plusMonths(1).minusDays(1);
    return withEndOfDate(quarterMonthLastDay.plusYears(1));
  }

  /**
   * 1년 만료 정책을 기반으로 만료일을 계산한다.
   *
   * @param from 시작 기준일
   * @return 만료일
   */
  public static LocalDateTime calculateNextYear(LocalDateTime from) {
    return withEndOfDate(from.plusYears(1));
  }
}
