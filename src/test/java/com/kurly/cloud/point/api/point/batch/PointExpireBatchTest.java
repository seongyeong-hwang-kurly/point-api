/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.batch;

import com.kurly.cloud.point.api.point.batch.expire.config.PointExpireJobConfig;
import com.kurly.cloud.point.api.point.domain.HistoryType;
import com.kurly.cloud.point.api.point.domain.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import com.kurly.cloud.point.api.point.service.port.in.PublishPointPort;
import java.time.LocalDateTime;
import java.util.Optional;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("PointExpireBatch class")
public class PointExpireBatchTest {
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

  long givenMemberNumber() {
    return 999999999;
  }

  int givenSize() {
    return 15;
  }

  @Nested
  @DisplayName("적립금 만료 배치를 실행 할 때")
  class DescribeExpire {
    @Nested
    @DisplayName("n개의 만료된 적립금이 있다면")
    class Context0 {

      void givenExpiredPoints() {
        long memberNumber = givenMemberNumber();

        for (int i = 0; i < givenSize(); i++) {
          publishPointPort.publish(PublishPointRequest.builder()
              .point(1000)
              .memberNumber(memberNumber)
              .historyType(HistoryType.TYPE_12.getValue())
              .actionMemberNumber(memberNumber)
              .expireDate(LocalDateTime.of(2020, 1, 1, 0, 0))
              .detail("지급")
              .build());
          memberNumber = memberNumber - 1;
        }
      }

      String givenExpireDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2020, 1, 2, 0, 0, 0);
        return dateTime.format(PointExpireJobConfig.DATE_TIME_FORMATTER);
      }

      @DisplayName("모두 만료 처리 된다")
      @Test
      void test() throws Exception {
        givenExpiredPoints();
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addString("expireTime", givenExpireDateTime());
        jobLauncher.run(pointExpireJob, jobParametersBuilder.toJobParameters());

        for (int i = 0; i < givenSize(); i++) {
          Optional<MemberPoint> memberPoint = memberPointRepository.findById(givenMemberNumber() - i);
          assertThat(memberPoint).isNotEmpty();
          assertThat(memberPoint.get().getTotalPoint()).isEqualTo(0);
        }
      }
    }

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

      tx.commit();
    }
  }
}
