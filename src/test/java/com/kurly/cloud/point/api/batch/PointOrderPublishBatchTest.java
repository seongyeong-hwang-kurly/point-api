package com.kurly.cloud.point.api.batch;

import com.kurly.cloud.point.api.batch.member.entity.Member;
import com.kurly.cloud.point.api.batch.member.repository.MemberRepository;
import com.kurly.cloud.point.api.batch.order.entity.Order;
import com.kurly.cloud.point.api.batch.order.entity.OrderDynamicColumn;
import com.kurly.cloud.point.api.batch.order.repository.OrderDynamicColumnRepository;
import com.kurly.cloud.point.api.batch.order.repository.OrderRepository;
import com.kurly.cloud.point.api.batch.publish.PointOrderPublishScheduler;
import com.kurly.cloud.point.api.batch.publish.config.PointOrderPublishJobConfig;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
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

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  OrderRepository orderRepository;

  @Autowired
  OrderDynamicColumnRepository orderDynamicColumnRepository;

  private long fromOrderNumber = randomOrderNumber();
  private long fromMemberNumber = givenMemberNumber();

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
    Member savedMember = memberRepository.findById(memberNumber).orElseThrow(()->new IllegalArgumentException("no member" + memberNumber));
    Order order = Order.builder()
        .orderNumber(orderNumber)
        .orderStatus(4)
        .orderProcessCode(0)
        .member(savedMember)
        .deliveredDateTime(deliveredDateTime)
        .payPrice(givenPayPrice())
        .publishPoint(givenPoint())
        .build();
    Order savedOrder = orderRepository.save(order);
    OrderDynamicColumn orderDynamicColumn = OrderDynamicColumn.builder()
        .orderNumber(savedOrder.getOrderNumber())
        .column("point_ratio")
        .value(String.valueOf(givenPointRatio()))
        .build();
    orderDynamicColumnRepository.save(orderDynamicColumn);
  }

  @Nested
  @Transactional
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
          givenOrder(randomOrderNumber() - i, givenMemberNumber() - i, givenDeliveredDate());
        });
        fromOrderNumber = randomOrderNumber() - size;
        fromMemberNumber = givenMemberNumber() - size;
      }

      @DisplayName("모두 적립 된다")
      @Test
      void test() {
        givenOrderBySize(1);
        subject();
        MemberPoint memberPoint = memberPointRepository.findById(givenMemberNumber()).get();

        int expectedPoint = givenPoint();
        assertThat(memberPoint.getTotalPoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getFreePoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }
  }

  long randomOrderNumber() {
    return new Random().nextLong();
  }

//  @Nested
//  @DisplayName("적립금 지급 배치를 실행 할 때")
//  class DescribeOrderPublish {
//    int givenPoint() {
//      return 1000;
//    }
//
//    LocalDateTime givenDeliveredDate() {
//      return LocalDateTime.of(2000, 1, 2, 12, 0);
//    }
//
//    void subject() throws Exception {
//      JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
//      jobParametersBuilder.addDate("now", new Date());
//      jobParametersBuilder.addString("publishDate",
//          givenDeliveredDate().plusDays(1).format(PointBatchConfig.DATE_TIME_FORMATTER));
//      jobLauncher.run(pointOrderPublishJob, jobParametersBuilder.toJobParameters());
//    }
//
//    @Nested
//    @DisplayName("적립 가능한 주문이 있다면")
//    class Context0 {
//      void givenOrderBySize(int size) {
//        IntStream.range(0, size).parallel().forEach(i -> {
//          givenOrder(ranadomOrderNumber() - i, givenMemberNumber() - i, givenDeliveredDate());
//        });
//        fromOrderNumber = ranadomOrderNumber() - size;
//        fromMemberNumber = givenMemberNumber() - size;
//      }
//
//      @DisplayName("모두 적립 된다")
//      @Test
//      void test() throws Exception {
//        givenOrderBySize(10);
//        subject();
//        MemberPoint memberPoint = memberPointRepository.findById(givenMemberNumber()).get();
//
//        int expectedPoint = givenPoint();
//        assertThat(memberPoint.getTotalPoint()).isEqualTo(expectedPoint);
//        assertThat(memberPoint.getFreePoint()).isEqualTo(expectedPoint);
//        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
//      }
//    }
//
//    @Nested
//    @DisplayName("배치가 1번 이상 실행되어도")
//    class Context1 {
//      @DisplayName("한번만 적립 된다")
//      @Test
//      void test() throws Exception {
//        givenOrder(ranadomOrderNumber(), givenMemberNumber(), givenDeliveredDate());
//        subject();
//        subject();
//        MemberPoint memberPoint = memberPointRepository.findById(givenMemberNumber()).get();
//
//        int expectedPoint = givenPoint();
//        assertThat(memberPoint.getTotalPoint()).isEqualTo(expectedPoint);
//        assertThat(memberPoint.getFreePoint()).isEqualTo(expectedPoint);
//        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
//      }
//    }
//  }
}
