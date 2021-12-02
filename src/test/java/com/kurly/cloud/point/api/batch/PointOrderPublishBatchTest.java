package com.kurly.cloud.point.api.batch;

import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
import com.kurly.cloud.point.api.batch.member.entity.Member;
import com.kurly.cloud.point.api.batch.order.entity.Order;
import com.kurly.cloud.point.api.batch.order.entity.OrderDynamicColumn;
import com.kurly.cloud.point.api.batch.publish.PointOrderPublishScheduler;
import com.kurly.cloud.point.api.batch.publish.config.PointOrderPublishJobConfig;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("It hasn't worked after 2021.10")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("PointOrderPublishBatch class")
public class PointOrderPublishBatchTest implements CommonTestGiven {
  @Autowired
  JobLauncher jobLauncher;

  @Autowired
  @Qualifier("pointOrderPublishJob")
  Job pointOrderPublishJob;

  @Autowired
  EntityManagerFactory entityManagerFactory;

  @Autowired
  PointOrderPublishJobConfig pointOrderPublishJobConfig;

  @Autowired
  MemberPointRepository memberPointRepository;

  private long fromOrderNumber = givenOrderNumber();
  private long fromMemberNumber = givenMemberNumber();

  @AfterEach
  void clear() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();

    entityManager
        .createQuery(
            "DELETE FROM Order o WHERE o.orderNumber BETWEEN :fromOrderNumber AND :toOrderNumber")
        .setParameter("fromOrderNumber", fromOrderNumber)
        .setParameter("toOrderNumber", givenOrderNumber())
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM OrderDynamicColumn odc WHERE odc.orderNumber BETWEEN :fromOrderNumber AND :toOrderNumber")
        .setParameter("fromOrderNumber", fromOrderNumber)
        .setParameter("toOrderNumber", givenOrderNumber())
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM MemberPoint mp WHERE mp.memberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", givenMemberNumber())
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM Point p WHERE p.memberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", givenMemberNumber())
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM PointHistory ph WHERE ph.orderNumber BETWEEN :fromOrderNumber AND :toOrderNumber")
        .setParameter("fromOrderNumber", fromOrderNumber)
        .setParameter("toOrderNumber", givenOrderNumber())
        .executeUpdate();

    entityManager
        .createQuery(
            "DELETE FROM MemberPointHistory ph WHERE ph.memberNumber BETWEEN :fromMemberNumber AND :toMemberNumber")
        .setParameter("fromMemberNumber", fromMemberNumber)
        .setParameter("toMemberNumber", givenMemberNumber())
        .executeUpdate();

    entityManager.close();
    tx.commit();
  }

  int givenPayPrice() {
    return 3333;
  }

  private Integer givenPointRatio() {
    return 7;
  }

  int givenPoint() {
    return 1000;
  }

  void givenOrder(long orderNumber, long memberNumber, LocalDateTime deliveredDateTime) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();
    Order order = Order.builder()
        .orderNumber(orderNumber)
        .orderStatus(4)
        .orderProcessCode(0)
        .member(Member.builder().memberNumber(memberNumber).build())
        .deliveredDateTime(deliveredDateTime)
        .payPrice(givenPayPrice())
        .publishPoint(givenPoint())
        .build();
    entityManager.persist(order);
    OrderDynamicColumn orderDynamicColumn = OrderDynamicColumn.builder()
        .orderNumber(order.getOrderNumber())
        .column("point_ratio")
        .value(String.valueOf(givenPointRatio()))
        .build();
    entityManager.persist(orderDynamicColumn);
    entityManager.close();
    tx.commit();
  }

  @Nested
  @DisplayName("적립금 지급 스케줄러를 실행 할 때")
  class DescribeOrderPublishSchedule {

    PointOrderPublishScheduler pointOrderPublishScheduler =
        new PointOrderPublishScheduler(pointOrderPublishJob, jobLauncher);
    
    LocalDateTime givenDeliveredDate() {
      return LocalDateTime.now().minusDays(1);
    }

    void subject() {
      pointOrderPublishScheduler.execute();
    }

    @Nested
    @DisplayName("적립 가능한 주문이 있다면")
    class Context0 {
      void givenOrderBySize(int size) {
        IntStream.range(0, size).parallel().forEach(i -> {
          givenOrder(givenOrderNumber() - i, givenMemberNumber() - i, givenDeliveredDate());
        });
        fromOrderNumber = givenOrderNumber() - size;
        fromMemberNumber = givenMemberNumber() - size;
      }

      @DisplayName("모두 적립 된다")
      @Test
      void test() {
        givenOrderBySize(10);
        subject();
        MemberPoint memberPoint = memberPointRepository.findById(givenMemberNumber()).get();

        int expectedPoint = givenPoint();
        assertThat(memberPoint.getTotalPoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getFreePoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }
  }

  @Nested
  @DisplayName("적립금 지급 배치를 실행 할 때")
  class DescribeOrderPublish {
    int givenPoint() {
      return 1000;
    }

    LocalDateTime givenDeliveredDate() {
      return LocalDateTime.of(2000, 1, 2, 12, 0);
    }

    void subject() throws Exception {
      JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
      jobParametersBuilder.addDate("now", new Date());
      jobParametersBuilder.addString("publishDate",
          givenDeliveredDate().plusDays(1).format(PointBatchConfig.DATE_TIME_FORMATTER));
      jobLauncher.run(pointOrderPublishJob, jobParametersBuilder.toJobParameters());
    }

    @Nested
    @DisplayName("적립 가능한 주문이 있다면")
    class Context0 {
      void givenOrderBySize(int size) {
        IntStream.range(0, size).parallel().forEach(i -> {
          givenOrder(givenOrderNumber() - i, givenMemberNumber() - i, givenDeliveredDate());
        });
        fromOrderNumber = givenOrderNumber() - size;
        fromMemberNumber = givenMemberNumber() - size;
      }

      @DisplayName("모두 적립 된다")
      @Test
      void test() throws Exception {
        givenOrderBySize(10);
        subject();
        MemberPoint memberPoint = memberPointRepository.findById(givenMemberNumber()).get();

        int expectedPoint = givenPoint();
        assertThat(memberPoint.getTotalPoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getFreePoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }

    @Nested
    @DisplayName("배치가 1번 이상 실행되어도")
    class Context1 {
      @DisplayName("한번만 적립 된다")
      @Test
      void test() throws Exception {
        givenOrder(givenOrderNumber(), givenMemberNumber(), givenDeliveredDate());
        subject();
        subject();
        MemberPoint memberPoint = memberPointRepository.findById(givenMemberNumber()).get();

        int expectedPoint = givenPoint();
        assertThat(memberPoint.getTotalPoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getFreePoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }
  }
}
