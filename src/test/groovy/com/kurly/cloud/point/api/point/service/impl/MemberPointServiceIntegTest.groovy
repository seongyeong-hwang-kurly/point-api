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
    PublishPointService publishPointService

    @Subject
    @Autowired
    MemberPointService memberPointService

    def setup() {
        historyRequest = MemberPointHistoryListRequest.builder().memberNumber(MEMBER_NO).build()
        expiredAt = LocalDateTime.parse("2021-12-31T23:59:59.99")
    }

    def 'should give point to a member'() {
        given:
        (1..3).forEach {
            def pointRequest = PublishPointRequest.builder()
                    .point(100).orderNumber(1)
                    .historyType(1)
                    .memberNumber(MEMBER_NO).expireDate(expiredAt).build()
            publishPointService.publish(pointRequest)
        }
        when:
        def pointHistories = memberPointService.getMemberHistoryList(historyRequest)
        then:
        pointHistories.size() == 3
    }

    def 'should return accumulated points summary'() {
        when:
        def summary = memberPointService.getMemberPointSummary(MEMBER_NO)
        then:
        summary.getAmount() == 300
        summary.getNextExpireAmount() == 0
        println(summary.getNextExpireDate())
    }

    def 'should return only accumulated member point'() {
        when:
        def point = memberPointService.getMemberPoint(MEMBER_NO)
        then:
        point.getTotalPoint() == 300
    }

}
