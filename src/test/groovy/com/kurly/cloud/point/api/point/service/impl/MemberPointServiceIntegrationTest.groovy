package com.kurly.cloud.point.api.point.service.impl

import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest
import com.kurly.cloud.point.api.point.domain.history.HistoryType
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest
import com.kurly.cloud.point.api.point.domain.publish.ReservationRequestParam
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
class MemberPointServiceIntegrationTest extends Specification {
    public static final int MEMBER_NO = 999999999
    public static final int ORDER_NUMBER = 1
    public static final int FIRST_TRY = 3
    public static final int SECOND_TRY = 2
    public static final int FIRST_UNIT_PRICE = 100
    public static final int SECOND_UNIT_PRICE = 200
    public static final int RESERVE_POINT = 9999
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
    PointReservationDomainService pointReservationDomainService

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
        summariseFromAvailablePoints() == getSummaryFromMemberPoint()
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
        summariseFromAvailablePoints() == getSummaryFromMemberPoint()
    }

    def '5. should reserve points not include in available points'(){
        when:
        def reserveVO = ReservationRequestParam.create(
                MEMBER_NO,
                ORDER_NUMBER,
                RESERVE_POINT,
                0.1f,
                HistoryType.TYPE_1.getValue(),
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
        pointReservationDomainService.reserve(reserveVO)
        then:
        pointReservationDomainService.sumReservedPoint(MEMBER_NO) == RESERVE_POINT
    }

    def '6. convert the reserved point to available point'() {
        given:
        def sumBeforeConversion = summariseFromAvailablePoints()
        def before = pointReservationDomainService.findPointReservations(MEMBER_NO).first()
        before.getPointEntity() == null
        !before.isApplied()
        when:
        pointReservationDomainService.transformIfReservedPointBefore(MEMBER_NO, LocalDateTime.now().plusDays(2))
        then:
        sumBeforeConversion + RESERVE_POINT == getSummaryFromMemberPoint()
        getSummaryFromMemberPoint() == summariseFromAvailablePoints()
        def reservations = pointReservationDomainService.findPointReservations(MEMBER_NO)
        reservations.size() == 0 // it's already transformed to a real point
    }

    private long getSummaryFromMemberPoint() {
        memberPointService.getMemberPoint(MEMBER_NO).getTotalPoint()
    }

    private long summariseFromAvailablePoints() {
        pointDomainService.getAvailableMemberPoint(MEMBER_NO).stream().mapToLong({ it -> it.getRemain() }).sum()
    }
}
