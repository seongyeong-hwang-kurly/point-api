package com.kurly.cloud.point.api.point.entity

import spock.lang.Specification

import java.time.LocalDateTime

class MemberPointCalculationTest extends Specification {
    def 'should return summed point'() {
        given:
        def point = new MemberPoint(1, 1000, 500, 500, LocalDateTime.now(), null)
        when:
        point.plusPoint(500, 0)
        then:
        point.getTotalPoint() == 1500
    }
}
