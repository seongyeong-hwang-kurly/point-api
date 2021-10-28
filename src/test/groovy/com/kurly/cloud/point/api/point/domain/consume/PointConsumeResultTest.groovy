package com.kurly.cloud.point.api.point.domain.consume

import com.kurly.cloud.point.api.point.exception.WrongPointRequestException
import spock.lang.Specification

class PointConsumeResultTest extends Specification {
    def "should return whether point is over"() {
        given:
        def consumeResult = new PointConsumeResult(5_000)

        when:
        consumeResult.add(CONSUMED_POINTS)

        then:
        CONSUMED_POINT == consumeResult.getTotalConsumed()
        IS_NOT_COMPLETE == consumeResult.isNotComplete()

        where:
        CONSUMED_POINTS          | CONSUMED_POINT || IS_NOT_COMPLETE
        getUnderConsumedPoints() | 3_000L         || true
        getOverConsumedPoints()  | 5_000L         || false
    }

    def "should throw WrongPointRequestException"() {
        given:
        def consumeResult = new PointConsumeResult(3_000)

        when:
        consumeResult.add(getOverConsumedPoints())

        then:
        thrown(WrongPointRequestException)
    }

    private static ArrayList<ConsumedPoint> getUnderConsumedPoints() {
        [
                new ConsumedPoint(1, 1000, false),
                new ConsumedPoint(2, 1000, false),
                new ConsumedPoint(3, 1000, false),
        ]
    }

    private static ArrayList<ConsumedPoint> getOverConsumedPoints() {
        [
                new ConsumedPoint(1, 1000, false),
                new ConsumedPoint(2, 1000, false),
                new ConsumedPoint(3, 3000, false),
        ]
    }
}
