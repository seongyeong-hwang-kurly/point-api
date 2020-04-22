package com.kurly.cloud.point.api.point.service.port.out;

import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryDto;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import org.springframework.data.domain.Page;

public interface MemberPointPort {
  /**
   * 회원의 포인트 이력을 조회 합니다
   */
  Page<MemberPointHistoryDto> getMemberHistoryList(MemberPointHistoryListRequest request);

  MemberPointSummary getMemberPointSummary(long memberNumber);
}
