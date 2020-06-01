package com.kurly.cloud.point.api.point.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.kurly.cloud.point.api.point.common.CommonTestGiven;
import com.kurly.cloud.point.api.point.common.TransactionalTest;
import com.kurly.cloud.point.api.point.domain.PointExpireResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.port.in.ExpirePointPort;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("ExpirePointAdapter class")
public class ExpirePointAdapterTest implements CommonTestGiven {

  @Autowired
  ExpirePointPort expirePointPort;

  @Autowired
  PublishPointPort publishPointPort;

  @Nested
  @DisplayName("회원의 적립금을 만료 시킬 때")
  class DescribeExpireMemberPoint {

    LocalDateTime givenExpiredDateTime() {
      return LocalDateTime.of(2020, 1, 1, 0, 0, 0);
    }

    LocalDateTime givenExpiredTargetDateTime() {
      return LocalDateTime.of(2020, 1, 2, 0, 0, 0);
    }

    LocalDateTime givenNonExpiredDateTime() {
      return LocalDateTime.of(2020, 1, 3, 0, 0, 0);
    }

    PointExpireResult subject() {
      return expirePointPort.expireMemberPoint(givenMemberNumber(), givenExpiredTargetDateTime());
    }

    int givenPointAmount() {
      return 1000;
    }

    void givenPoint(LocalDateTime expireDate) {
      publishPointPort.publish(PublishPointRequest.builder()
          .point(givenPointAmount())
          .memberNumber(givenMemberNumber())
          .historyType(HistoryType.TYPE_12.getValue())
          .actionMemberNumber(givenMemberNumber())
          .expireDate(expireDate)
          .detail("지급")
          .build());
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료된 적립금이 있다면")
    class Context0 {

      @DisplayName("적립금이 만료처리 된다")
      @Test
      public void test() {
        givenPoint(givenExpiredDateTime());
        PointExpireResult pointExpireResult = subject();

        assertThat(pointExpireResult.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(pointExpireResult.getTotalExpired()).isEqualTo(givenPointAmount());
      }
    }

    @TransactionalTest
    @Nested
    @DisplayName("만료된 적립금이 없다면")
    class Context1 {
      @DisplayName("적립금이 만료처리 되지않는다")
      @Test
      public void test() {
        givenPoint(givenNonExpiredDateTime());
        PointExpireResult pointExpireResult = subject();

        assertThat(pointExpireResult.getMemberNumber()).isEqualTo(givenMemberNumber());
        assertThat(pointExpireResult.getTotalExpired()).isEqualTo(0);
      }
    }
  }
}
