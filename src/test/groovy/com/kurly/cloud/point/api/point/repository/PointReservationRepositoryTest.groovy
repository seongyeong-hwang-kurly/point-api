package com.kurly.cloud.point.api.point.repository

import com.kurly.cloud.point.api.point.entity.Point
import com.kurly.cloud.point.api.point.entity.PointReservation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.lang.Subject

import javax.transaction.Transactional
import java.time.LocalDateTime

@SpringBootTest
class PointReservationRepositoryTest extends Specification {
    @Subject
    @Autowired
    PointReservationRepository pointReservationRepository

    @Autowired
    PointRepository pointRepository

    @Transactional
    def 'should get id not 0 after saving PointReservation'() {
        given: "initially, construct a point reservation "
        def pointReservation = PointReservation.builder().seq(0)
                .memberNumber(1).remain(1000)
                .charge(1000).historyType(1)
                .startedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now())
                .build()

        when: "save it, and find it again for check"
        def saved = pointReservationRepository.save(pointReservation)
        def found = pointReservationRepository.findById(saved.getSeq())

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
        def foundAfterApply = pointReservationRepository.findById(saved.getSeq())
        foundAfterApply.get().getPoint() != null
        found.get().isApplied()
    }
}
