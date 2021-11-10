package com.kurly.cloud.point.api.point.service.impl

import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest
import com.kurly.cloud.point.api.point.domain.history.HistoryType
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest
import com.kurly.cloud.point.api.point.domain.publish.PublishPointReserveRequestVO
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import java.time.LocalDateTime
import java.time.ZonedDateTime

@Stepwise
@SpringBootTest
@ActiveProfiles("local")
class MemberPointServiceIntegTest extends Specification {
    public static final int MEMBER_NO = 999999999
    public static final int ORDER_NUMBER = 1
    public static final int FIRST_TRY = 3
    public static final int SECOND_TRY = 2
    public static final int FIRST_UNIT_PRICE = 100
    public static final int SECOND_UNIT_PRICE = 200
    @Shared
    MemberPointHistoryListRequest historyRequest
    @Shared
    LocalDateTime expiredAt

    @Subject
    @Autowired
    PublishPointServiceV2 publishPointService

    @Subject
    @Autowired
    ConsumePointServiceV2 consumePointService

    @Subject
    @Autowired
    MemberPointService memberPointService

    @Subject
    @Autowired
    PointDomainService pointDomainService

    @Subject
    @Autowired
    PointReserveDomainService pointReserveDomainService

    def setup() {
        historyRequest = MemberPointHistoryListRequest.builder().memberNumber(MEMBER_NO).build()
        expiredAt = LocalDateTime.parse("2021-12-31T23:59:59.99")
    }

    def '1. should give point to a member(/v1/publish)'() {
        given:
        publishNTimes(FIRST_TRY, FIRST_UNIT_PRICE)
        publishNTimes(SECOND_TRY, SECOND_UNIT_PRICE)
        when:
        def pointHistories = memberPointService.getMemberHistoryList(historyRequest)
        then:
        pointHistories.size() == FIRST_TRY + SECOND_TRY
        summaryFromAvailablePoints() == summaryFromMemberPoint()
    }

    private publishNTimes(int times, int point) {
        (1..times).forEach {
            def pointRequest = PublishPointRequest.builder()
                    .point(point).orderNumber(ORDER_NUMBER)
                    .historyType(1)
                    .memberNumber(MEMBER_NO).expireDate(expiredAt).build()
            publishPointService.publish(pointRequest)
        }
    }

    def '2. should return available member point (/v1/available/{memberNumber})'() {
        when:
        def memberPoint = memberPointService.getMemberPoint(MEMBER_NO)
        then:
        memberPoint.getTotalPoint() == FIRST_TRY * FIRST_UNIT_PRICE + SECOND_TRY * SECOND_UNIT_PRICE
    }

    def '3. should be able to use the points(/v1/consume/order)'() {
        given:
        def orderRequest = OrderConsumePointRequest.builder()
            .orderNumber(ORDER_NUMBER)
            .memberNumber(MEMBER_NO)
            .point(350)
            .build()
        when:
        consumePointService.consumeByOrder(orderRequest)
        then:
        350L == memberPointService.getMemberPoint(MEMBER_NO).getTotalPoint()
    }

    def '4. should refund point already used(/v1/consume/cancel)'() {
        given:
        def cancelRequest = CancelOrderConsumePointRequest.builder()
                .memberNumber(MEMBER_NO)
                .point(300)
                .orderNumber(ORDER_NUMBER)
                .build()
        when:
        consumePointService.cancelConsumeByOrder(cancelRequest)
        then:
        summaryFromAvailablePoints() == summaryFromMemberPoint()
    }

    def '5. should reserve points not include in available points'(){
        when:
        def reserveVO = PublishPointReserveRequestVO.create(
                MEMBER_NO,
                ORDER_NUMBER,
                1000,
                0.1f,
                HistoryType.TYPE_1,
                true,
                false,
                false,
                ZonedDateTime.now().plusYears(1),
                StringUtils.EMPTY,
                "test point",
                MEMBER_NO,
                false,
                LocalDateTime.now().plusDays(1)
        )
        pointReserveDomainService.reserve(reserveVO)
        then:
        noExceptionThrown()
    }

    private long summaryFromMemberPoint() {
        memberPointService.getMemberPoint(MEMBER_NO).getTotalPoint()
    }

    private long summaryFromAvailablePoints() {
        pointDomainService.getAvailableMemberPoint(MEMBER_NO).stream().mapToLong({ it -> it.getRemain() }).sum()
    }
}
