package com.kurly.cloud.point.api.point.service.helper

import spock.lang.Specification

class DivideUsingFreePointHelperTest extends Specification {

    def '비율에 맞게 포인트 금액을 계산한다.'() {
        given:
        def totalPoint = 100
        def totalPrice = 1000
        when:
        def proportionalPoint = DivideUsingFreePointHelper.getProportionalPoint(totalPrice, EACHPRICE, totalPoint)
        then:
        proportionalPoint == EXPECT
        where:
        EACHPRICE || EXPECT
        300       || 30
        400       || 40
        500       || 50
    }

}
