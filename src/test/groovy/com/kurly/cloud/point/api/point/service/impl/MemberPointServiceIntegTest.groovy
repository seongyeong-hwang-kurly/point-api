package com.kurly.cloud.point.api.point.service.impl

import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import java.time.LocalDateTime

@Stepwise
@SpringBootTest
@ActiveProfiles("local")
class MemberPointServiceIntegTest extends Specification {
    public static final int MEMBER_NO = 999999999
    @Shared
    MemberPointHistoryListRequest historyRequest
    @Shared
    LocalDateTime expiredAt

    @Subject
    @Autowired
    PublishPointServiceV2 publishPointService

    @Subject
    @Autowired
    MemberPointService memberPointService

    def setup() {
        historyRequest = MemberPointHistoryListRequest.builder().memberNumber(MEMBER_NO).build()
        expiredAt = LocalDateTime.parse("2021-12-31T23:59:59.99")
    }

    def '1. should give point to a member'() {
        given:
        publishNTimes(3, 100)
        publishNTimes(1, 300)
        when:
        def pointHistories = memberPointService.getMemberHistoryList(historyRequest)
        then:
        pointHistories.size() == 4
    }

    private publishNTimes(int times, int point) {
        (1..times).forEach {
            def pointRequest = PublishPointRequest.builder()
                    .point(point).orderNumber(1)
                    .historyType(1)
                    .memberNumber(MEMBER_NO).expireDate(expiredAt).build()
            publishPointService.publish(pointRequest)
        }
    }

    def '2. should return accumulated points summary'() {
        when:
        def summary = memberPointService.getMemberPointSummary(MEMBER_NO)
        then:
        summary.getAmount() == 600
        summary.getNextExpireAmount() == 0
        println(summary.getNextExpireDate())
    }

    def '3. should return available member point'() {
        when:
        def point = memberPointService.getMemberPoint(MEMBER_NO)
        then:
        point.getTotalPoint() == 600
    }

}
