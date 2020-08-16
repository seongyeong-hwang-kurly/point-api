package com.kurly.cloud.point.api.recommend.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum RecommendationPointStatus {
  DEFAULT("기본", 0),
  PAID("지급", 1),
  NON_PAID("미지급", 2),
  DEDUCTED("회수", 3);

  private String desc;
  private int value;

  RecommendationPointStatus(String desc, int value) {
    this.desc = desc;
    this.value = value;
  }

  public static RecommendationPointStatus getByValue(int value) {
    return Arrays.stream(RecommendationPointStatus.values())
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
