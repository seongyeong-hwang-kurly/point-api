package com.kurly.cloud.point.api.recommend.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum RecommendationDelayType {
  DELAYED("미검증", 1),
  CHECKED("검증", 2);

  private String desc;
  private int value;

  RecommendationDelayType(String desc, int value) {
    this.desc = desc;
    this.value = value;
  }

  public static RecommendationDelayType getByValue(int value) {
    return Arrays.stream(RecommendationDelayType.values())
        .filter(type -> type.getValue().equals(value))
        .findAny()
        .orElseThrow(
            () -> new IllegalArgumentException(String.format("타입([%s])이 존재하지 않습니다.", value)));
  }

  @JsonValue
  public Integer getValue() {
    return value;
  }
}
