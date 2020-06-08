package com.kurly.cloud.point.api.point.port.out;

import com.kurly.cloud.point.api.point.adapter.out.dto.MemberPointHistoryDto;
import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import org.springframework.data.domain.Page;

public interface MemberPointPort {
  /**
   * 회원의 적립금 이력을 조회 합니다.
   */
  Page<MemberPointHistoryDto> getMemberHistoryList(MemberPointHistoryListRequest request);

  MemberPointSummary getMemberPointSummary(long memberNumber);
}
