package com.kurly.cloud.point.api.point.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public class DateTimeUtil {
  /**
   * LocalDateTime 을 ZonedDateTime 으로 변환 한다.
   */
  public static ZonedDateTime toZonedDateTime(LocalDateTime localDateTime) {
    if (Objects.isNull(localDateTime)) {
      return null;
    }
    return ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
  }

  /**
   * ZonedDateTime 을 LocalDateTime 으로 변환 한다.
   */
  public static LocalDateTime toLocalDateTime(ZonedDateTime zonedDateTime) {
    if (Objects.isNull(zonedDateTime)) {
      return null;
    }
    return zonedDateTime.toLocalDateTime();
  }

  /**
   * LocalDateTime을 unixtimestamp 로 변환한다.
   */
  public static long toTimestamp(LocalDateTime localDateTime) {
    if (Objects.isNull(localDateTime)) {
      return 0;
    }
    return Timestamp.valueOf(localDateTime).getTime();
  }
}
