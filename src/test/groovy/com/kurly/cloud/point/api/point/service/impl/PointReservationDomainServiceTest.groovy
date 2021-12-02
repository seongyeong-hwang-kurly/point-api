package com.kurly.cloud.point.api.point.service.impl

import com.kurly.cloud.point.api.point.repository.PointReservationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import spock.lang.Specification

@ActiveProfiles("local")
@SpringBootTest
class PointReservationDomainServiceTest extends Specification {
    @Autowired
    PointReservationRepository pointReservationRepository
    @Autowired
    PointReservationDomainService pointReservationDomainService

    @Sql("/sql/insert_reserved_points.sql")
    def 'should return reserved points already saved'(){
        given:
        def memberNumber = 1237654
        when:
        def allReserved = pointReservationRepository.findAllByMemberNumber(memberNumber)
        then:
        allReserved.size() == 5
        when:
        def yetApplied = pointReservationDomainService.getReservedPoints(memberNumber)
        then:
        yetApplied.size() == 4
        yetApplied.every({!it.isApplied()})
    }
}
