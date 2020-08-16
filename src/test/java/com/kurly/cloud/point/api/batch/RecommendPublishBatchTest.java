package com.kurly.cloud.point.api.batch;

import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
import com.kurly.cloud.point.api.member.entity.Member;
import com.kurly.cloud.point.api.member.repository.MemberRepository;
import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.order.repository.OrderRepository;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("RecommendPublishBatch class")
public class RecommendPublishBatchTest implements CommonTestGiven {
  @Autowired
  JobLauncher jobLauncher;

  @Autowired
  @Qualifier("recommendPublishJob")
  Job recommendPublishJob;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  OrderRepository orderRepository;

  void subject() throws Exception {
    JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
    jobParametersBuilder.addString("deliveredDate", givenDeliveredDate());
    jobLauncher.run(recommendPublishJob, jobParametersBuilder.toJobParameters());
  }

  String givenDeliveredDate() {
    return LocalDateTime.now().format(PointBatchConfig.DATE_TIME_FORMATTER);
  }

  Member givenRecommenderMember(int index) {
    Member member = Member.builder()
        .memberId("recommend" + index)
        .mobile("010-4321-4321")
        .name("추천인" + index)
        .memberUuid(UUID.randomUUID().toString())
        .recommendMemberId("")
        .build();
    memberRepository.saveAndFlush(member);
    return member;
  }

  Member givenRecommendeeMember(String recommenderId, int index) {
    Member member = Member.builder()
        .memberId("recommendee" + index)
        .mobile("010-3321-4321")
        .name("추천자" + index)
        .memberUuid(UUID.randomUUID().toString())
        .recommendMemberId(recommenderId)
        .build();
    memberRepository.saveAndFlush(member);
    return member;
  }

  Order givenDeliveredOrder(Member orderMember, LocalDateTime delivered) {
    Order order = Order.builder()
        .orderNumber(new Date().getTime())
        .member(orderMember)
        .orderStatus(4)
        .deliveredDateTime(delivered)
        .jibunFullAddress("ADDRESS ADDRESS SUB")
        .address("ADDRESS")
        .addressSub("ADDRESS SUB")
        .roadFullAddress("ROAD ADDRESS")
        .mobile(orderMember.getMobile())
        .build();
    orderRepository.saveAndFlush(order);
    return order;
  }

  @Disabled("Rollback을 구현 해야 함")
  @Test
  void test() throws Exception {
    Member recommender = givenRecommenderMember(0);
    Member recommendee = givenRecommendeeMember(recommender.getMemberId(), 0);
    Order order = givenDeliveredOrder(recommendee, LocalDateTime.now());
    subject();
  }
}
