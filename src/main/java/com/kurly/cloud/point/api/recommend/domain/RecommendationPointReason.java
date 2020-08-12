package com.kurly.cloud.point.api.recommend.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum RecommendationPointReason {
  DEFAULT("기본", 0),
  NO_RECOMMENDER_ID("추천인 미기재", 1),
  NOT_EXIST_RECOMMENDER("추천인 아이디 미존재", 2),
  SAME_PHONE_NUMBER("휴대폰 번호 일치", 3),
  INVALID_RECOMMENDEE_PHONE_NUMBER("주문인 휴대폰 번호 오류", 4),
  INVALID_RECOMMENDER_PHONE_NUMBER("추천인 휴대폰 번호 오류", 5),
  INVALID_PHONE_NUMBER("주문인,추천인 휴대폰 번호 오류", 6),
  DUPLICATE_ADDRESS("추천인 유사 주소 지급 횟수 초과", 7),
  NOT_EXIST_RECOMMENDEE("주문인 회원 아이디 미존재", 8),
  NOT_EXIST_RECOMMENDEE_ADDRESS("주문인 주소 미존재", 9),
  NO_FIRST_ORDER("첫주문이 아님", 10),
  BLACKLIST("어뷰징 블랙리스트", 11);

  private String desc;
  private int value;

  RecommendationPointReason(String desc, int value) {
    this.desc = desc;
    this.value = value;
  }

  public static RecommendationPointReason getByValue(int value) {
    return Arrays.stream(RecommendationPointReason.values())
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
