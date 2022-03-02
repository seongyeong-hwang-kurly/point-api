package com.kurly.cloud.point.api.batch.recommend.repository

import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDataType
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDelayType
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointStatus
import com.kurly.cloud.point.api.batch.recommend.entity.RecommendationPointHistory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("local")
class RecommendationPointHistoryRepositoryTest extends Specification {
    public static final long MEMBER_NUMBER = 4321
    @Autowired
    RecommendationPointHistoryRepository recommendationPointHistoryRepository

    @Transactional
    def "should give a query with the changed signature"() {
        given:
        List<RecommendationPointHistory> histories = new ArrayList<RecommendationPointHistory>()
        (1..3).forEach {
            RecommendationPointHistory created = RecommendationPointHistory.builder()
                    .orderNumber(it * 10)
                    .orderMemberNumber(MEMBER_NUMBER)
                    .status(RecommendationPointStatus.DEDUCTED)
                    .type(RecommendationDataType.PRODUCTION_DATA)
                    .delayType(RecommendationDelayType.CHECKED)
                    .recommendationMemberNumber(it % 2 == 0 ? null : it)
                    .point(1000)
                    .orderAddress("ADDRESS")
                    .orderPhoneNumber("010-4321-4321")
                    .createDateTime(LocalDateTime.now())
                    .updateDateTime(LocalDateTime.now())
                    .status(RecommendationPointStatus.PAID)
                    .build()
            histories.add(created)
        }

        recommendationPointHistoryRepository.saveAll(histories)
        when:
        def allSavedHistories = recommendationPointHistoryRepository.findAll()
        then:
        allSavedHistories.size() == 3
        when:
        def foundHistories = recommendationPointHistoryRepository
                .findAllByOrderMemberNumberAndStatusAndRecommendationMemberNumberIsNotNull(MEMBER_NUMBER, RecommendationPointStatus.PAID)
        then:
        foundHistories.size() == 2
    }
}
