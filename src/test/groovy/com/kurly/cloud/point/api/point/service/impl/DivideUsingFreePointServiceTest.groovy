package com.kurly.cloud.point.api.point.service.impl

import com.kurly.cloud.point.api.point.param.DealProductRequestParam
import com.kurly.cloud.point.api.point.param.DealProductResponseParam
import com.kurly.cloud.point.api.point.param.DivideUsingFreePointRequestParam
import com.kurly.cloud.point.api.point.service.DivideUsingFreePointService
import spock.lang.Specification
import spock.lang.Subject

class DivideUsingFreePointServiceTest extends Specification {
    @Subject
    DivideUsingFreePointService divideService

    def setup() {
        divideService = new DivideUsingFreePointService()
    }

    def "Split"() {
        given:
        def param = DivideUsingFreePointRequestParam.create(
                1000, 100, 0,
                [DealProductRequestParam.create(1, 1000)]
        )
        when:
        List<DealProductResponseParam> dealProducts = divideService.divide(param)

        then:
        dealProducts.size() > 0
    }

    def "Split2"() {
        given:
        def param = DivideUsingFreePointRequestParam.create(
                1000, 100, 0,
                [
                        DealProductRequestParam.create(1, 500),
                        DealProductRequestParam.create(2, 500)
                ]
        )
        when:
        List<DealProductResponseParam> dealProducts = divideService.divide(param)

        then:
        dealProducts.size() == 2
        dealProducts.each {
            assert it.getUsedFreePoint() == 50
        }
    }

    def "Split3"() {
        given:
        def param = DivideUsingFreePointRequestParam.create(
                1000, 100, 0,
                [
                        DealProductRequestParam.create(1, 300),
                        DealProductRequestParam.create(2, 300),
                        DealProductRequestParam.create(3, 400)
                ]
        )
        when:
        List<DealProductResponseParam> dealProducts = divideService.divide(param)

        then:
        dealProducts.size() == 3
        dealProducts.get(0).getUsedFreePoint() == 30
        dealProducts.get(1).getUsedFreePoint() == 30
        dealProducts.get(2).getUsedFreePoint() == 40
    }

    def "Split4"() {
        given:
        def param = DivideUsingFreePointRequestParam.create(
                17900, 2010, 0,
                [
                        DealProductRequestParam.create(1, 1894),
                        DealProductRequestParam.create(2, 10938),
                        DealProductRequestParam.create(3, 4262),
                        DealProductRequestParam.create(4,806)
                ]
        )
        when:
        List<DealProductResponseParam> dealProducts = divideService.divide(param)

        then:
        dealProducts.size() == 4
        dealProducts.get(0).getUsedFreePoint() == 213
        dealProducts.get(1).getUsedFreePoint() == 1229
        dealProducts.get(2).getUsedFreePoint() == 478
        dealProducts.get(3).getUsedFreePoint() == 90
        dealProducts.stream().mapToInt({ it -> it.getUsedFreePoint() }).sum() == 2010
    }



}
