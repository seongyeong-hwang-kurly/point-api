package com.kurly.cloud.point.api.point.domain.publish;

import com.kurly.cloud.point.api.point.entity.PointReservationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "create")
public class ReservationResultParam {
  private long id;
  private Long memberNumber;
  private Long reservedPoint;
  private Integer historyType;
  private boolean payment;
  private boolean settle;
  private boolean applied;
  private LocalDateTime startedAt;
  private LocalDateTime createdAt;
  private LocalDateTime expiredAt;

  public static ReservationResultParam from(PointReservationEntity entity){
    return ReservationResultParam.create(
            entity.getId(), entity.getMemberNumber(), entity.getPoint(),
             entity.getHistoryType(), entity.isPayment(),
            entity.isSettle(), entity.isApplied(), entity.getStartedAt(),
            entity.getCreatedAt(), entity.getExpireDate());
  }
}
