package com.kurly.cloud.point.api.point.domain.publish;

import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.entity.PointReservation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
public class PublishPointReservationRequestVO extends PublishPointRequest {
  private boolean applied;
  private LocalDateTime startedAt;

  private PublishPointReservationRequestVO(long memberNumber,
                                           long orderNumber,
                                           long point,
                                           float pointRatio,
                                           HistoryType historyType,
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
    super(memberNumber, orderNumber, point, pointRatio, historyType.getValue(), payment, settle, unlimitedDate, expireDate, memo, detail, actionMemberNumber, hidden);
    this.applied = applied;
    this.startedAt = startedAt;
  }

  public static PublishPointReservationRequestVO create(long memberNumber,
                                                        long orderNumber,
                                                        long point,
                                                        float pointRatio,
                                                        HistoryType historyType,
                                                        boolean payment,
                                                        boolean settle,
                                                        boolean unlimitedDate,
                                                        ZonedDateTime expireDate,
                                                        String memo,
                                                        String detail,
                                                        long actionMemberNumber,
                                                        boolean hidden,
                                                        LocalDateTime startedAt) {
    validate(startedAt);
    return new PublishPointReservationRequestVO(
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

  private static void validate(LocalDateTime startedAt) {
    if(LocalDateTime.now().isAfter(startedAt)){
      throw new IllegalArgumentException("startAt must be future");
    }
  }

  public PointReservation convertToEntity() {
    return PointReservation.builder()
            .id(0)
            .memberNumber(memberNumber)
            .orderNumber(orderNumber)
            .charge(point)
            .remain(point)
            .pointRatio(pointRatio)
            .historyType(historyType)
            .refundType(1)
            .payment(false)
            .settle(false)
            .startedAt(this.startedAt)
            .expiredAt(expireDate.toLocalDateTime())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }
}
