package com.kurly.cloud.point.api.point.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import java.time.ZonedDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublishResultDto {
  long seq;
  Long memberNumber;
  long orderNumber;
  Long charge;
  float pointRatio;
  Integer historyType;
  boolean payment;
  boolean settle;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime regDateTime;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
  ZonedDateTime expireDateTime;

  /**
   * Entity에서 인스턴스 생성.
   */
  public static PublishResultDto fromEntity(Point point) {
    return PublishResultDto.builder()
        .seq(point.getSeq())
        .memberNumber(point.getMemberNumber())
        .orderNumber(point.getOrderNumber())
        .charge(point.getCharge())
        .pointRatio(point.getPointRatio())
        .historyType(point.getHistoryType())
        .payment(point.isPayment())
        .settle(point.isSettle())
        .regDateTime(DateTimeUtil.toZonedDateTime(point.getRegTime()))
        .expireDateTime(DateTimeUtil.toZonedDateTime(point.getExpireTime()))
        .build();
  }
}
