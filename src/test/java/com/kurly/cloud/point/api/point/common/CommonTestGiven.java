package com.kurly.cloud.point.api.point.common;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface CommonTestGiven {
  default long givenMemberNumber() {
    return Long.parseLong(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhh")));
  }

  default long givenStaticMemberNumber() {
    return 999999999;
  }

  default long givenOrderNumber() {
    return 88888888;
  }
}
