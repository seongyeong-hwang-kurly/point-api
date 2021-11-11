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
        given:
        def pointReservation = PointReservation.builder().seq(0)
                .memberNumber(1).remain(1000)
                .charge(1000).historyType(1)
                .startedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now())
                .build()
        when:
        def saved = pointReservationRepository.save(pointReservation)
        def found = pointReservationRepository.findById(saved.getSeq())
        then:
        found.isPresent()
        found.get().isApplied() == false
        when:
        def point = Point.builder().seq(0)
                .memberNumber(1).remain(1000)
                .charge(1000).historyType(1)
                .build()
        def savedPoint = pointRepository.save(point)
        pointReservation.apply(point)
        pointReservationRepository.save(pointReservation)
        then:
        def foundAfterApply = pointReservationRepository.findById(saved.getSeq())
        foundAfterApply.get().getPoint() != null
        found.get().isApplied() == true
    }
}
