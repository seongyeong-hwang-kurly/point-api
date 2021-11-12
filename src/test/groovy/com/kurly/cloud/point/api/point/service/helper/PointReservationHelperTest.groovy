package com.kurly.cloud.point.api.point.service.helper

import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest
import com.kurly.cloud.point.api.point.entity.PointReservationEntity
import spock.lang.Specification

import java.time.LocalDateTime

class PointReservationHelperTest extends Specification {
    def 'should convert from PointReservation to PublishPointRequest'() {
        given:
        def reservation = PointReservationEntity.builder()
                .memberNumber(1)
                .orderNumber(1)
                .charge(1000)
                .remain(1000)
                .pointRatio(0.1)
                .historyType(1)
                .refundType(1)
                .payment(false)
                .settle(false)
                .startedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        when:
        PublishPointRequest request = PointReservationHelper.convert(reservation)
        then:
        noExceptionThrown()
    }
}

