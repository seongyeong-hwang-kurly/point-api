package com.kurly.cloud.point.api.point.domain.publish;

import com.kurly.cloud.point.api.point.entity.PointReservationEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
public class ReservationRequestParam extends PublishPointRequest {
  private boolean applied;
  private LocalDateTime startedAt;

  private ReservationRequestParam(long memberNumber,
                                  long orderNumber,
                                  long point,
                                  float pointRatio,
                                  int historyType,
                                  boolean payment,
                                  boolean settle,
                                  boolean unlimitedDate,
                                  ZonedDateTime expireDate,
                                  String memo,
                                  String detail,
                                  long actionMemberNumber,
                                  boolean hidden,
                                  boolean applied,
                                  LocalDateTime startedAt) {
    super(memberNumber, orderNumber, point, pointRatio, historyType, payment, settle, unlimitedDate, expireDate, memo, detail, actionMemberNumber, hidden);
    this.applied = applied;
    this.startedAt = startedAt;
  }

  public static ReservationRequestParam create(long memberNumber,
                                               long orderNumber,
                                               long point,
                                               float pointRatio,
                                               int historyType,
                                               boolean payment,
                                               boolean settle,
                                               boolean unlimitedDate,
                                               ZonedDateTime expireDate,
                                               String memo,
                                               String detail,
                                               long actionMemberNumber,
                                               boolean hidden,
                                               LocalDateTime startedAt) {
    return new ReservationRequestParam(
      memberNumber,
      orderNumber,
      point,
      pointRatio,
      historyType,
      payment,
      settle,
      unlimitedDate,
      expireDate,
      memo,
      detail,
      actionMemberNumber,
      hidden,
      false,
      startedAt
    );
  }

  public PointReservationEntity convertToEntity() {
    return PointReservationEntity.builder()
            .id(0)
            .memberNumber(memberNumber)
            .point(point)
            .historyType(historyType)
            .payment(payment)
            .settle(settle)
            .expireDate(expireDate.toLocalDateTime())
            .memo(memo)
            .detail(detail)
            .actionMemberNumber(actionMemberNumber)
            .hidden(hidden)
            .applied(applied)
            .startedAt(startedAt)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }
}
