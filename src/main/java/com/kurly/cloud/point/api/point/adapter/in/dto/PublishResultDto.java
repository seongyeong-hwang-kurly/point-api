package com.kurly.cloud.point.api.point.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kurly.cloud.point.api.point.entity.Point;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublishResultDto {
  long seq;
  Long memberNumber;
  long orderNumber;
  Integer charge;
  float pointRatio;
  Integer historyType;
  boolean payment;
  boolean settle;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime regDateTime;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime expireDateTime;

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
        .regDateTime(point.getRegTime())
        .expireDateTime(point.getExpireTime())
        .build();
  }
}
