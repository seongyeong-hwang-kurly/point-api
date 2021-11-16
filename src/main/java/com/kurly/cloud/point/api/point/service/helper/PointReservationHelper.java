package com.kurly.cloud.point.api.point.service.helper;

import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.PointReservationEntity;

public class PointReservationHelper {
    public static PublishPointRequest convert(PointReservationEntity reservation) {
        return PublishPointRequest.builder()
                .memberNumber(reservation.getMemberNumber())
                .orderNumber(reservation.getOrderNumber())
                .point(reservation.getPoint())
                .pointRatio(reservation.getPointRatio())
                .historyType(reservation.getHistoryType())
                .payment(reservation.isPayment())
                .settle(reservation.isSettle())
                .unlimitedDate(reservation.isUnlimitedDate())
                .expireDate(reservation.getExpireDate())
                .memo(reservation.getMemo())
                .detail(reservation.getDetail())
                .actionMemberNumber(reservation.getActionMemberNumber())
                .hidden(reservation.isHidden())
                .build();
    }
}
