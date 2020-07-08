package com.kurly.cloud.point.api.point.common;

public interface CommonTestGiven {
  default long givenMemberNumber() {
    return 999999999;
  }

  default long givenOrderNumber() {
    return 88888888;
  }
}
