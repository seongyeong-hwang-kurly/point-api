package com.kurly.cloud.point.api.point.repository

import com.kurly.cloud.point.api.point.entity.Point
import com.kurly.cloud.point.api.point.entity.PointReservationEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
import spock.lang.Subject

import javax.transaction.Transactional
import java.time.LocalDateTime
import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles("local")
class PointReservationRepositoryTest extends Specification {
    @Subject
    @Autowired
    PointReservationRepository pointReservationRepository

    @Autowired
    PointRepository pointRepository

    @Transactional
    def 'should get id not 0 after saving PointReservation'() {
        given: "initially, construct a point reservation "
        def pointReservation = PointReservationEntity.builder()
                .id(0)
                .memberNumber(1)
                .point(1000)
                .historyType(1)
                .expireDate(ZonedDateTime.now())
                .startedAt(LocalDateTime.now())
                .build()

        when: "save it, and find it again for check"
        def saved = pointReservationRepository.save(pointReservation)
        def found = pointReservationRepository.findById(saved.getId())

        then:  "should find the saved one and not yet applied"
        found.isPresent()
        !found.get().isApplied()

        when: "apply new point to the previous point reserve"
        def point = Point.builder().seq(0)
                .memberNumber(1).remain(1000)
                .charge(1000).historyType(1)
                .build()
        pointRepository.save(point)
        pointReservation.apply(point)
        pointReservationRepository.save(pointReservation)

        then: "should return it with the point and be applied"
        def foundAfterApply = pointReservationRepository.findById(saved.getId())
        foundAfterApply.get().getPoint() != 0
        found.get().isApplied()
    }
}
