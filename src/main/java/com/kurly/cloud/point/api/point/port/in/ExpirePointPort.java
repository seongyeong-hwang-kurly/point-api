package com.kurly.cloud.point.api.point.port.in;

import com.kurly.cloud.point.api.point.domain.PointExpireResult;
import java.time.LocalDateTime;

public interface ExpirePointPort {
  /**
   * 회원이 가지고 있는 만료된 적립금을 만료 처리 합니다
   */
  PointExpireResult expireMemberPoint(long memberNumber, LocalDateTime expireTime);
}
