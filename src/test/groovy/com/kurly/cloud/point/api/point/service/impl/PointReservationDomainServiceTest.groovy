package com.kurly.cloud.point.api.point.service.impl


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class PointReservationDomainServiceTest extends Specification {
    @Autowired
    PointReservationDomainService pointReservationDomainService

    def 'should return reserved points already saved'(){
        given:
        def memberNumber = 1234567
        when:
        def points = pointReservationDomainService.getReservedPoints(memberNumber)
        then:
        !points.isEmpty()
    }
}
