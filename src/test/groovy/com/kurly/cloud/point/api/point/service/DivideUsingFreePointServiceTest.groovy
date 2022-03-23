package com.kurly.cloud.point.api.point.service

import com.kurly.cloud.point.api.point.service.DivideUsingFreePointService
import com.kurly.cloud.point.api.point.web.dto.DealProductRequestDto
import com.kurly.cloud.point.api.point.web.dto.DealProductResponseDto
import com.kurly.cloud.point.api.point.web.dto.DivideUsingFreePointRequestDto
import spock.lang.Specification
import spock.lang.Subject

class DivideUsingFreePointServiceTest extends Specification {
    @Subject
    DivideUsingFreePointService divideService

    def setup() {
        divideService = new DivideUsingFreePointService()
    }

    def "Divide"() {
        given:
        def param = DivideUsingFreePointRequestDto.create(
                1000, 100, 0,
                [DealProductRequestDto.create(1, null, 1000)]
        )
        when:
        List<DealProductResponseDto> dealProducts = divideService.divide(param)

        then:
        dealProducts.size() > 0
        
    }

    def "Divide2"() {
        given:
        def param = DivideUsingFreePointRequestDto.create(
                1000, 100, 0,
                [
                        DealProductRequestDto.create(1, null, 500),
                        DealProductRequestDto.create(2, null, 500)
                ]
        )
        when:
        List<DealProductResponseDto> dealProducts = divideService.divide(param)

        then:
        dealProducts.size() == 2
        dealProducts.each {
            assert it.getUsedFreePoint() == 50
        }
    }

    def "Divide3"() {
        given:
        def param = DivideUsingFreePointRequestDto.create(
                1000, 100, 0,
                [
                        DealProductRequestDto.create(1, null, 300),
                        DealProductRequestDto.create(2, null, 300),
                        DealProductRequestDto.create(3, null, 400)
                ]
        )
        when:
        List<DealProductResponseDto> dealProducts = divideService.divide(param)

        then:
        dealProducts.size() == 3
        dealProducts.get(0).getUsedFreePoint() == 30
        dealProducts.get(1).getUsedFreePoint() == 30
        dealProducts.get(2).getUsedFreePoint() == 40
    }

    def "Divide4"() {
        given:
        def param = DivideUsingFreePointRequestDto.create(
                17900, 2010, 0,
                [
                        DealProductRequestDto.create(1, null, 1894),
                        DealProductRequestDto.create(2, null, 10938),
                        DealProductRequestDto.create(3, null, 4262),
                        DealProductRequestDto.create(4, null,806)
                ]
        )
        when:
        List<DealProductResponseDto> dealProducts = divideService.divide(param)

        then:
        dealProducts.size() == 4
        dealProducts.get(0).getUsedFreePoint() == 213
        dealProducts.get(1).getUsedFreePoint() == 1229
        dealProducts.get(2).getUsedFreePoint() == 478
        dealProducts.get(3).getUsedFreePoint() == 90
        dealProducts.stream().mapToInt({ it -> it.getUsedFreePoint() }).sum() == 2010
    }

    def "요청한 usingFreePoint와 계산된 무상 적립금이 다르면, 에러를 던진다" (){
        given:
        def param = DivideUsingFreePointRequestDto.create(
                37900, 3644, 0,
                [
                        DealProductRequestDto.create(13415, null, 17498),
                        DealProductRequestDto.create(14213, null, 6318),
                        DealProductRequestDto.create(13433, null, 7292),
                        DealProductRequestDto.create(13433, null, 3792)
                ]
        )
        when:
        List<DealProductResponseDto> dealProducts = divideService.divide(param)
        then:
        def e = thrown(IllegalArgumentException.class)
        e.message == "요청한 usingFreePoint와 계산된 무상적립금이 일치하지 않습니다."
    }



}
