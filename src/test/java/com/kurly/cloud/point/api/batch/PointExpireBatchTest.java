package com.kurly.cloud.point.api.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.kurly.cloud.point.api.batch.expire.config.PointExpireJobConfig;
import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;
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
@DisplayName("PointExpireBatch class")
public class PointExpireBatchTest implements CommonTestGiven {
  @Autowired
  JobLauncher jobLauncher;

  @Autowired
  @Qualifier("pointExpireJob")
  Job pointExpireJob;

  @Autowired
  PublishPointPort publishPointPort;

  @Autowired
  MemberPointRepository memberPointRepository;

  @Autowired
  EntityManagerFactory entityManagerFactory;

  int givenSize() {
    return 15;
  }

  @Nested
  @DisplayName("적립금 만료 배치를 실행 할 때")
  class DescribeExpire {
    @AfterEach
    void clear() {
      long startMemberNumber = givenMemberNumber() - givenSize();

      EntityManager entityManager = entityManagerFactory.createEntityManager();
      EntityTransaction tx = entityManager.getTransaction();
      tx.begin();

      entityManager
          .createQuery("DELETE FROM MemberPoint mp WHERE mp.memberNumber >= :memberNumber")
          .setParameter("memberNumber", startMemberNumber)
          .executeUpdate();

      entityManager
          .createQuery("DELETE FROM Point p WHERE p.memberNumber >= :memberNumber")
          .setParameter("memberNumber", startMemberNumber)
          .executeUpdate();

      entityManager
          .createQuery("DELETE FROM MemberPointHistory ph WHERE ph.memberNumber >= :memberNumber")
          .setParameter("memberNumber", startMemberNumber)
          .executeUpdate();

      entityManager
          .createQuery("DELETE FROM PointHistory ph WHERE ph.actionMemberNumber >= :memberNumber")
          .setParameter("memberNumber", startMemberNumber)
          .executeUpdate();

      tx.commit();
    }

    @Nested
    @DisplayName("n개의 만료된 적립금이 있다면")
    class Context0 {

      @DisplayName("모두 만료 처리 된다")
      @Test
      void test() throws Exception {
        givenExpiredPoints();
        subject();

        for (int i = 0; i < givenSize(); i++) {
          Optional<MemberPoint> memberPoint =
              memberPointRepository.findById(givenMemberNumber() - i);
          assertThat(memberPoint).isNotEmpty();
          assertThat(memberPoint.get().getTotalPoint()).isEqualTo(0);
        }
      }

      void givenExpiredPoints() throws ExecutionException, InterruptedException {
        new ForkJoinPool(100).submit(() -> {
          IntStream.range(0, givenSize()).parallel().forEach(i -> {
            long memberNumber = givenMemberNumber() - i;
            publishPointPort.publish(PublishPointRequest.builder()
                .point(1000)
                .memberNumber(memberNumber)
                .historyType(HistoryType.TYPE_12.getValue())
                .actionMemberNumber(memberNumber)
                .expireDate(LocalDateTime.of(2020, 1, 1, 0, 0))
                .detail("지급")
                .build());
          });
        }).get();
      }

      void subject() throws Exception {
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("expireTime", givenExpireDateTime());
        jobLauncher.run(pointExpireJob, jobParametersBuilder.toJobParameters());
      }

      String givenExpireDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2020, 1, 2, 0, 0, 0);
        return dateTime.format(PointExpireJobConfig.DATE_TIME_FORMATTER);
      }
    }
  }
}
