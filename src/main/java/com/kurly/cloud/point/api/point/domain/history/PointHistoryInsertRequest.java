package com.kurly.cloud.point.api.point.domain.history;

import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointHistoryInsertRequest {

  Long pointSeq;
  long orderNumber;
  Long amount;
  Integer historyType;
  @Builder.Default
  String detail = "";
  @Builder.Default
  String memo = "";
  boolean settle;
  long actionMemberNumber;

  /**
   * Entity로 변환.
   */
  public PointHistory toEntity() {
    return PointHistory.builder()
        .point(Objects.isNull(pointSeq) ? null : Point.builder().seq(pointSeq).build())
        .actionMemberNumber(actionMemberNumber)
        .amount(amount)
        .detail(detail)
        .historyType(historyType)
        .memo(memo)
        .orderNumber(orderNumber)
        .regTime(LocalDateTime.now())
        .settle(settle)
        .build();
  }

}
