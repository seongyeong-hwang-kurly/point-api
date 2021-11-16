package com.kurly.cloud.point.api.batch;

import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
import com.kurly.cloud.point.api.batch.member.entity.Member;
import com.kurly.cloud.point.api.batch.member.repository.MemberRepository;
import com.kurly.cloud.point.api.batch.order.entity.Order;
import com.kurly.cloud.point.api.batch.order.repository.OrderRepository;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDataType;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationDelayType;
import com.kurly.cloud.point.api.batch.recommend.domain.RecommendationPointStatus;
import com.kurly.cloud.point.api.batch.recommend.entity.RecommendationPointHistory;
import com.kurly.cloud.point.api.batch.recommend.repository.RecommendationPointHistoryRepository;
import com.kurly.cloud.point.api.batch.recommend.service.RecommendationPointHistoryUseCase;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import com.kurly.cloud.point.api.point.service.ConsumePointUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@TestPropertySource(properties = {"batch.recommend.chunkSize=100", "batch.recommend.poolSize=10"})
@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("RecommendPublishBatch class")
public class RecommendPublishBatchTest implements CommonTestGiven {
  @Autowired
  EntityManagerFactory entityManagerFactory;

  EntityManager entityManager;

  @Autowired
  JobLauncher jobLauncher;

  @Autowired
  @Qualifier("recommendPublishJob")
  Job recommendPublishJob;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  OrderRepository orderRepository;

  @Autowired
  MemberPointRepository memberPointRepository;

  @Autowired
  RecommendationPointHistoryUseCase recommendationPointHistoryUseCase;

  @Autowired
  RecommendationPointHistoryRepository recommendationPointHistoryRepository;

  @Autowired
  ConsumePointUseCase consumePointUseCase;

  List<Long> memberNumbers = new ArrayList<>();
  List<Long> orderNumbers = new ArrayList<>();
  List<Long> orderMemberNumbers = new ArrayList<>();

  @BeforeEach
  void initializeEntityManager() {
    this.entityManager = entityManagerFactory.createEntityManager();
  }

  JobExecution subject() throws Exception {
    JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
    jobParametersBuilder.addString("deliveredDate", givenDeliveredDate());
    return jobLauncher.run(recommendPublishJob, jobParametersBuilder.toJobParameters());
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
    orderMemberNumbers.add(member.getMemberNumber());
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

  void createData(int size, boolean createMember) {
    IntStream.range(0, size).forEach(index -> {
      EntityTransaction tx = entityManager.getTransaction();
      tx.begin();
      Member recommendee;
      if (createMember) {
        Member recommender = givenRecommenderMember(index);
        recommendee = givenRecommendeeMember(recommender.getMemberId(), index);
      } else {
        recommendee = memberRepository.findByMemberId("recommendee" + index).get();
      }
      givenDeliveredOrder(recommendee, LocalDateTime.now());
      tx.commit();
    });
  }

  int givenSize() {
    return 10;
  }

  @DisplayName("친구 초대 적립금 지급")
  @Test
  void test() throws Exception {
    createData(givenSize(), true);
    JobExecution subject = subject();
    ExecutionContext executionContext = subject.getExecutionContext();
    long totalOrderCount = executionContext.getLong("totalOrderCount");
    long totalValidCount = executionContext.getLong("totalValidCount");
    long totalPublishPointAmount = executionContext.getLong("totalPublishPointAmount");
    long totalPublishPointCount = executionContext.getLong("totalPublishPointCount");
    assertThat(totalOrderCount).isEqualTo(givenSize());
    assertThat(totalValidCount).isEqualTo(givenSize());
    assertThat(totalPublishPointCount).isEqualTo(givenSize() * 2);
    assertThat(totalPublishPointAmount)
        .isEqualTo(givenSize() * 2 * recommendationPointHistoryUseCase.getPaidPoint());
    List<MemberPoint> memberPoints = memberPointRepository.findAllById(memberNumbers);
    assertThat(memberPoints.size()).isEqualTo(givenSize() * 2);
    memberPoints.forEach(memberPoint -> {
      assertThat(memberPoint.getTotalPoint())
          .isEqualTo(recommendationPointHistoryUseCase.getPaidPoint());
    });
  }

  @DisplayName("주문을 동시에 여러개 한 경우 한번만 지급 되어야 한다")
  @Test
  void test1() throws Exception {
    createData(givenSize(), true);
    createData(givenSize(), false);
    JobExecution subject = subject();
    ExecutionContext executionContext = subject.getExecutionContext();
    long totalOrderCount = executionContext.getLong("totalOrderCount");
    long totalValidCount = executionContext.getLong("totalValidCount");
    long totalPublishPointAmount = executionContext.getLong("totalPublishPointAmount");
    long totalPublishPointCount = executionContext.getLong("totalPublishPointCount");
    assertThat(totalOrderCount).isEqualTo(givenSize());
    assertThat(totalValidCount).isEqualTo(givenSize());
    assertThat(totalPublishPointCount).isEqualTo(givenSize() * 2);
    assertThat(totalPublishPointAmount).isEqualTo(givenSize() * 2 *
        recommendationPointHistoryUseCase.getPaidPoint());
    List<MemberPoint> memberPoints = memberPointRepository.findAllById(memberNumbers);
    assertThat(memberPoints.size()).isEqualTo(givenSize() * 2);
    memberPoints.forEach(memberPoint -> {
      assertThat(memberPoint.getTotalPoint())
          .isEqualTo(recommendationPointHistoryUseCase.getPaidPoint());
    });
  }

  @DisplayName("한번 지급 받은 회원이 다시 주문을 하면 지급되지 않아야 한다")
  @Test
  void test2() throws Exception {
    createData(givenSize(), true);
    subject();

    clearOrder();
    createData(givenSize(), false);
    subject();

    List<MemberPoint> memberPoints = memberPointRepository.findAllById(memberNumbers);
    assertThat(memberPoints.size()).isEqualTo(givenSize() * 2);
    memberPoints.forEach(memberPoint -> {
      assertThat(memberPoint.getTotalPoint())
          .isEqualTo(recommendationPointHistoryUseCase.getPaidPoint());
    });
  }

  @DisplayName("지급 후 회수가 되었을 경우 다시 주문을 하면 지급이 되어야 한다")
  @Test
  void test3() throws Exception {
    createData(givenSize(), true);
    JobExecution subject = subject();
    List<MemberPoint> memberPoints = memberPointRepository.findAllById(memberNumbers);
    assertThat(memberPoints.size()).isEqualTo(givenSize() * 2);
    memberPoints.forEach(memberPoint -> {
      assertThat(memberPoint.getTotalPoint())
          .isEqualTo(recommendationPointHistoryUseCase.getPaidPoint());
    });
    entityManager.clear();

    deductRecommendPaidPoint(orderNumbers, orderMemberNumbers);
    memberPoints = memberPointRepository.findAllById(orderMemberNumbers);
    memberPoints.forEach(memberPoint -> {
      assertThat(memberPoint.getTotalPoint()).isEqualTo(0);
    });

    clearOrder();
    entityManager.clear();
    createData(givenSize(), false);
    subject();
    memberPoints = memberPointRepository.findAllById(orderMemberNumbers);
    memberPoints.forEach(memberPoint -> {
      assertThat(memberPoint.getTotalPoint())
          .isEqualTo(recommendationPointHistoryUseCase.getPaidPoint());
    });
  }

  private void deductRecommendPaidPoint(List<Long> orderNumbers, List<Long> memberNumbers)
      throws NotEnoughPointException {
    int size = memberNumbers.size();
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();
    for (int i = 0; i < size; i++) {
      RecommendationPointHistory deducted = RecommendationPointHistory.builder()
          .orderNumber(orderNumbers.get(i))
          .orderMemberNumber(memberNumbers.get(i))
          .status(RecommendationPointStatus.DEDUCTED)
          .type(RecommendationDataType.PRODUCTION_DATA)
          .delayType(RecommendationDelayType.CHECKED)
          .point(-recommendationPointHistoryUseCase.getPaidPoint())
          .orderAddress("ADDRESS")
          .orderPhoneNumber("010-4321-4321")
          .createDateTime(LocalDateTime.now())
          .updateDateTime(LocalDateTime.now())
          .build();
      recommendationPointHistoryRepository.save(deducted);
      consumePointUseCase.consume(ConsumePointRequest.builder()
          .memberNumber(memberNumbers.get(i))
          .point((long) recommendationPointHistoryUseCase.getPaidPoint())
          .historyType(HistoryType.TYPE_102.getValue())
          .detail("")
          .build());
    }
    tx.commit();
  }

  void clearOrder() {
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

    tx.commit();
  }

  void clearPoint() {
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();

    long fromOrderNumber = Collections.min(orderNumbers);
    long toOrderNumber = Collections.max(orderNumbers);
    long fromMemberNumber = Collections.min(memberNumbers);
    long toMemberNumber = Collections.max(memberNumbers);

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

    tx.commit();
  }

  void clearMember() {
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();

    long fromMemberNumber = Collections.min(memberNumbers);
    long toMemberNumber = Collections.max(memberNumbers);

    entityManager
        .createQuery(
            "DELETE FROM Member m WHERE m.memberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", toMemberNumber)
        .executeUpdate();

    tx.commit();
  }

  @AfterEach
  void clear() {
    clearOrder();
    clearPoint();
    clearMember();
    entityManager.clear();
    entityManager.close();
  }
}
