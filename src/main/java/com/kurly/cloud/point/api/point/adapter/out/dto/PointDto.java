package com.kurly.cloud.point.api.point.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointDto {
  long seq;
  Long memberNumber;
  long orderNumber;
  Integer charge;
  Integer remain;
  float pointRatio;
  Integer historyType;
  int refundType;
  boolean payment;
  boolean settle;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime regTime;
  long regTimestamp;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime expireTime;
  long expireTimestamp;

  /**
   * Entity에서 인스턴스 생성.
   */
  public static PointDto fromEntity(Point point) {
    return PointDto.builder()
        .seq(point.getSeq())
        .memberNumber(point.getMemberNumber())
        .orderNumber(point.getOrderNumber())
        .charge(point.getCharge())
        .remain(point.getRemain())
        .pointRatio(point.getPointRatio())
        .historyType(point.getHistoryType())
        .refundType(point.getRefundType())
        .payment(point.isPayment())
        .settle(point.isSettle())
        .regTime(DateTimeUtil.toZonedDateTime((point.getRegTime())))
        .regTimestamp(Timestamp.valueOf(point.getRegTime()).getTime())
        .expireTime(DateTimeUtil.toZonedDateTime(point.getExpireTime()))
        .expireTimestamp(Timestamp.valueOf(point.getExpireTime()).getTime())
        .build();
  }
}
