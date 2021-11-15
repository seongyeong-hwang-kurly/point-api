package com.kurly.cloud.point.api.point.service.helper

import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest
import com.kurly.cloud.point.api.point.entity.PointReservationEntity
import spock.lang.Specification

import java.time.LocalDateTime

class PointReservationHelperTest extends Specification {

    public static final int MEMBER_NUMBER = 1004
    public static final int ORDER_NUMBER = 999999
    public static final int POINT = 1000
    public static final float POINT_RATIO = 0.1f
    public static final int HISTORY_TYPE = 1

    def 'should convert from PointReservation to PublishPointRequest'() {
        given:
        def reservation = PointReservationEntity.builder()
                .memberNumber(MEMBER_NUMBER)
                .point(POINT)
                .historyType(HISTORY_TYPE)
                .payment(false)
                .settle(false)
                .expireDate(LocalDateTime.now().plusDays(7))
                .startedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        when:
        PublishPointRequest request = PointReservationHelper.convert(reservation)
        then:
        with(request) {
            assert memberNumber.longValue() == MEMBER_NUMBER
            assert point.longValue() == POINT
            assert historyType.intValue() == HISTORY_TYPE
        }
    }
}

