package com.kurly.cloud.point.api.point.service.impl

import com.kurly.cloud.point.api.point.entity.Point
import spock.lang.Specification

import java.time.LocalDateTime

class ExpirePointServiceHelperTest extends Specification {
    def 'should an exception when it comes to no expiredAt' () {
        given:
        def points = [Point.builder().expiredAt(null).build()]
        when:
        def expiredAt = ExpirePointServiceHelper.getLatestExpiredAt(points)
        def now = LocalDateTime.now()
        then:
        now.minusMinutes(1).isBefore(expiredAt) &&
                now.plusMinutes(1).isAfter(expiredAt)

    }

    def 'should get the latest expireDate from points' () {
        given:
        def givenLatestAt = LocalDateTime.now().plusDays(3)
        def points = [
                Point.builder().expiredAt(LocalDateTime.now().plusDays(1)).build(),
                Point.builder().expiredAt(LocalDateTime.now().plusDays(2)).build(),
                Point.builder().expiredAt(givenLatestAt).build()
        ]
        when:
        def latestDate = ExpirePointServiceHelper.getLatestExpiredAt(points)
        then:
        latestDate == givenLatestAt
    }
}
