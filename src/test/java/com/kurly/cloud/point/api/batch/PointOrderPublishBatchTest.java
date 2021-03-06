package com.kurly.cloud.point.api.batch;

import com.kurly.cloud.point.api.batch.config.PointBatchConfig;
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
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
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

  int givenPayPrice() {
    return 3333;
  }

  private Integer givenPointRatio() {
    return 7;
  }

  int givenPoint() {
    return 1000;
  }

  long newMemberNumber = 0;

  long givenOrder(long orderNumber, long memberNumber, LocalDateTime deliveredDateTime) {
    Member member = Member.builder()
            .memberNumber(givenMemberNumber())
            .memberId("memberId")
            .memberUuid("memberUuid")
            .recommendMemberId("test")
            .mobile("01011112222")
            .name("?????????")
            .build();
    var savedMember = memberRepository.save(member);

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

   return savedMember.getMemberNumber();
  }

  @Nested
  @DisplayName("????????? ?????? ??????????????? ?????? ??? ???")
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
    @DisplayName("?????? ????????? ????????? ?????????")
    class Context0 {
      void givenOrderBySize() {
        newMemberNumber = givenOrder(randomOrderNumber(), givenMemberNumber(), givenDeliveredDate());
      }

      @DisplayName("?????? ?????? ??????")
      @Test
      void test() {
        givenOrderBySize();
        subject();
        MemberPoint memberPoint = memberPointRepository.findAllByMemberNumber(newMemberNumber).get(0);

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

  @Nested
  @DisplayName("????????? ?????? ????????? ?????? ??? ???")
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
    @DisplayName("?????? ????????? ????????? ?????????")
    class Context0 {
      long givenOrderBySize() {
        return givenOrder(randomOrderNumber(), givenMemberNumber(), givenDeliveredDate());
      }

      @DisplayName("?????? ?????? ??????")
      @Test
      void test() throws Exception {
        newMemberNumber = givenOrderBySize();
        subject();
        MemberPoint memberPoint = memberPointRepository.findById(newMemberNumber).get();

        int expectedPoint = givenPoint();
        assertThat(memberPoint.getTotalPoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getFreePoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }

    @Nested
    @DisplayName("????????? 1??? ?????? ???????????????")
    class Context1 {
      @DisplayName("????????? ?????? ??????")
      @Test
      void test() throws Exception {
        newMemberNumber = givenOrder(randomOrderNumber(), givenMemberNumber(), givenDeliveredDate());
        subject();
        subject();
        MemberPoint memberPoint = memberPointRepository.findById(newMemberNumber).get();

        int expectedPoint = givenPoint();
        assertThat(memberPoint.getTotalPoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getFreePoint()).isEqualTo(expectedPoint);
        assertThat(memberPoint.getCashPoint()).isEqualTo(0);
      }
    }
  }
}
