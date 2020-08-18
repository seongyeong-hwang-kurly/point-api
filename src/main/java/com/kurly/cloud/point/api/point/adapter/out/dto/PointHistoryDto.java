package com.kurly.cloud.point.api.point.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointHistoryDto {
  long seq;
  long pointSeq;
  long orderNumber;
  int amount;
  int historyType;
  String detail;
  String memo;
  boolean settle;
  long actionMemberNumber;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime regDateTime;

  /**
   * Entity로 dto를 생성한다.
   */
  public static PointHistoryDto fromEntity(PointHistory pointHistory) {
    return PointHistoryDto.builder()
        .seq(pointHistory.getSeq())
        .pointSeq(pointHistory.getPoint().getSeq())
        .orderNumber(pointHistory.getOrderNumber())
        .amount(pointHistory.getAmount())
        .historyType(pointHistory.getHistoryType())
        .detail(pointHistory.getDetail())
        .memo(pointHistory.getMemo())
        .settle(pointHistory.isSettle())
        .actionMemberNumber(pointHistory.getActionMemberNumber())
        .regDateTime(DateTimeUtil.toZonedDateTime(pointHistory.getRegTime()))
        .build();
  }
}
