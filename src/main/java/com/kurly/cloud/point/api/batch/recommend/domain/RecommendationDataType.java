package com.kurly.cloud.point.api.batch.recommend.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum RecommendationDataType {
  MIGRATION_DATA("마이그레이션", 1),
  PRODUCTION_DATA("운영", 2);

  private String desc;
  private int value;

  RecommendationDataType(String desc, int value) {
    this.desc = desc;
    this.value = value;
  }

  /**
   * GetByValue.
   */
  public static RecommendationDataType getByValue(int value) {
    return Arrays.stream(RecommendationDataType.values())
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
