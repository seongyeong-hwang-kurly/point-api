package com.kurly.cloud.point.api.point.service.impl

import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

@SpringBootTest
class MemberPointServiceIntegTest extends Specification {
    public static final int MEMBER_NO = 999999999
    @Shared
    MemberPointHistoryListRequest memberPointRequest
    @Subject
    @Autowired
    MemberPointService memberPointService

    def setup() {
        memberPointRequest = MemberPointHistoryListRequest.builder().memberNumber(MEMBER_NO).build()
    }

    @Sql("/sql/insert_point.sql")
    def 'should return member point histories'() {
        when:
        def pointHistories = memberPointService.getMemberHistoryList(memberPointRequest)
        then:
        pointHistories.size() == 4
    }

    def 'should return accumulated points'() {
        when:
        def summary = memberPointService.getMemberPointSummary(MEMBER_NO)
        then:
        summary.getAmount() == 50
    }

}
