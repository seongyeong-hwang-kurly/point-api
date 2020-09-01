package com.kurly.cloud.point.api.batch;

import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
import com.kurly.cloud.point.api.member.entity.Member;
import com.kurly.cloud.point.api.member.repository.MemberRepository;
import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.order.repository.OrderRepository;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestPropertySource(properties = {"batch.recommend.chunkSize=100", "batch.recommend.poolSize=10"})
@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("RecommendPublishBatch class")
public class RecommendPublishBatchTest implements CommonTestGiven {
  @Autowired
  EntityManagerFactory entityManagerFactory;

  @Autowired
  JobLauncher jobLauncher;

  @Autowired
  @Qualifier("recommendPublishJob")
  Job recommendPublishJob;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  OrderRepository orderRepository;

  List<Long> memberNumbers = new ArrayList<>();
  List<Long> orderNumbers = new ArrayList<>();

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
    memberRepository.save(member);
    memberNumbers.add(member.getMemberNumber());
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
    memberRepository.save(member);
    memberNumbers.add(member.getMemberNumber());
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
    orderRepository.save(order);
    orderNumbers.add(order.getOrderNumber());
    return order;
  }

  void createData(int size) {
    IntStream.range(0, size).forEach(index -> {
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      EntityTransaction tx = entityManager.getTransaction();
      tx.begin();
      Member recommender = givenRecommenderMember(index);
      Member recommendee = givenRecommendeeMember(recommender.getMemberId(), index);
      givenDeliveredOrder(recommendee, LocalDateTime.now());
      tx.commit();
      entityManager.close();
    });
  }

  @DisplayName("친구 초대 적립금 지급 배치 테스트")
  @Test
  void test() throws Exception {
    createData(10);
    subject();
  }

  @AfterEach
  void clear() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();

    long fromOrderNumber = Collections.min(orderNumbers);
    long toOrderNumber = Collections.max(orderNumbers);

    entityManager
        .createQuery(
            "DELETE FROM Order o WHERE o.orderNumber BETWEEN :fromOrderNumber AND :toOrderNumber")
        .setParameter("fromOrderNumber", fromOrderNumber)
        .setParameter("toOrderNumber", toOrderNumber)
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM PointHistory ph WHERE ph.orderNumber BETWEEN :fromOrderNumber AND :toOrderNumber")
        .setParameter("fromOrderNumber", fromOrderNumber)
        .setParameter("toOrderNumber", toOrderNumber)
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM RecommendationPointHistory rh WHERE rh.orderNumber BETWEEN :fromOrderNumber AND :toOrderNumber")
        .setParameter("fromOrderNumber", fromOrderNumber)
        .setParameter("toOrderNumber", toOrderNumber)
        .executeUpdate();

    long fromMemberNumber = Collections.min(memberNumbers);
    long toMemberNumber = Collections.max(memberNumbers);

    entityManager
        .createQuery(
            "DELETE FROM Member m WHERE m.memberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", toMemberNumber)
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM RecommendationPointHistory rh WHERE rh.orderMemberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", toMemberNumber)
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM RecommendationPointHistory rh WHERE rh.recommendationMemberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", toMemberNumber)
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM MemberPoint mp WHERE mp.memberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", toMemberNumber)
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM Point p WHERE p.memberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", toMemberNumber)
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM MemberPointHistory ph WHERE ph.memberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", toMemberNumber)
        .executeUpdate();

    tx.commit();
  }
}
