package com.kurly.cloud.point.api.point.batch;

import static org.assertj.core.api.Assertions.assertThat;


import com.kurly.cloud.point.api.order.entity.Order;
import com.kurly.cloud.point.api.order.entity.OrderDynamicColumn;
import com.kurly.cloud.point.api.point.batch.publish.config.PointOrderPublishJobConfig;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import com.kurly.cloud.point.api.point.util.PointCalculator;
import java.time.LocalDateTime;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

  @AfterEach
  void clear() {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();

    entityManager
        .createQuery("DELETE FROM Order o WHERE o.orderNumber = :orderNumber")
        .setParameter("orderNumber", givenOrderNumber())
        .executeUpdate();

    entityManager
        .createQuery("DELETE FROM OrderDynamicColumn odc WHERE odc.orderNumber = :orderNumber")
        .setParameter("orderNumber", givenOrderNumber())
        .executeUpdate();

    entityManager
        .createQuery("DELETE FROM MemberPoint mp WHERE mp.memberNumber = :memberNumber")
        .setParameter("memberNumber", givenMemberNumber())
        .executeUpdate();

    entityManager
        .createQuery("DELETE FROM Point p WHERE p.memberNumber = :memberNumber")
        .setParameter("memberNumber", givenMemberNumber())
        .executeUpdate();

    entityManager
        .createQuery("DELETE FROM PointHistory ph WHERE ph.orderNumber = :orderNumber")
        .setParameter("orderNumber", givenOrderNumber())
        .executeUpdate();

    entityManager
        .createQuery("DELETE FROM MemberPointHistory ph WHERE ph.memberNumber = :memberNumber")
        .setParameter("memberNumber", givenMemberNumber())
        .executeUpdate();

    tx.commit();
  }

  @Nested
  @DisplayName("적립금 지급 배치를 실행 할 때")
  class DescribeOrderPublish {
    int givenPoint() {
      return PointCalculator.calculateOrderPoint(givenPayPrice(), givenPointRatio());
    }

    int givenPayPrice() {
      return 3333;
    }

    float givenPointRatio() {
      return 5;
    }

    private void givenOrder() {
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      EntityTransaction tx = entityManager.getTransaction();
      tx.begin();
      Order order = Order.builder()
          .orderNumber(givenOrderNumber())
          .orderStatus(1)
          .orderProcessCode(0)
          .memberNumber(givenMemberNumber())
          .payDateTime(givenPayDate())
          .payPrice(givenPayPrice())
          .build();
      entityManager.persist(order);
      OrderDynamicColumn orderDynamicColumn = OrderDynamicColumn.builder()
          .orderNumber(order.getOrderNumber())
          .column("point_ratio")
          .value(String.valueOf(givenPointRatio()))
          .build();
      entityManager.persist(orderDynamicColumn);
      tx.commit();
    }

    LocalDateTime givenPayDate() {
      return LocalDateTime.of(2000, 1, 2, 12, 0);
    }

    void subject() throws Exception {
      JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
      jobParametersBuilder.addDate("now", new Date());
      jobParametersBuilder.addString("publishDate",
          givenPayDate().plusDays(1).format(PointOrderPublishJobConfig.DATE_TIME_FORMATTER));
      jobLauncher.run(pointOrderPublishJob, jobParametersBuilder.toJobParameters());
    }

    @Nested
    @DisplayName("적립 가능한 주문이 있다면")
    class Context0 {
      @DisplayName("모두 적립 된다")
      @Test
      void test() throws Exception {
        givenOrder();
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
        givenOrder();
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
