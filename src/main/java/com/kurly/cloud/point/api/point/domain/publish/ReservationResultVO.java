package com.kurly.cloud.point.api.point.domain.publish;

import com.kurly.cloud.point.api.point.entity.PointReservationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
public class ReservationResultVO {
  private long id;
  private Long memberNumber;
  private long orderNumber;
  private Long reservedPoint;
  private float pointRatio;
  private Integer historyType;
  private boolean payment;
  private boolean settle;
  private boolean applied;
  private LocalDateTime startedAt;
  private LocalDateTime createdAt;
  private LocalDateTime expiredAt;

  public static ReservationResultVO from(PointReservationEntity entity){
    return ReservationResultVO.create(
            entity.getId(), entity.getMemberNumber(), entity.getOrderNumber(), entity.getPoint(),
            entity.getPointRatio(), entity.getHistoryType(), entity.isPayment(),
            entity.isSettle(), entity.isApplied(), entity.getStartedAt(),
            entity.getCreatedAt(), entity.getExpireDate());
  }
}
