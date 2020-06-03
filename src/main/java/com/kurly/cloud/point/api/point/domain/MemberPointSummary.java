package com.kurly.cloud.point.api.point.domain;

import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberPointSummary {
  int amount;
  LocalDateTime nextExpireDate;
  int nextExpireAmount;

  /**
   * 만료포인트가 없는 인스턴스를 생성한다.
   */
  public static MemberPointSummary byEmptyExpireAmount(int pointAmount) {
    return MemberPointSummary.builder()
        .amount(pointAmount)
        .nextExpireAmount(0)
        .nextExpireDate(PointExpireDateCalculator.calculateDefault(LocalDateTime.now()))
        .build();
  }
}
