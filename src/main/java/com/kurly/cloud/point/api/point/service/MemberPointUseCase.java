package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.web.dto.MemberPointHistoryDto;
import org.springframework.data.domain.Page;

public interface MemberPointUseCase {
  /**
   * 회원의 적립금 이력을 조회 합니다.
   */
  Page<MemberPointHistoryDto> getMemberHistoryList(MemberPointHistoryListRequest request);

  MemberPointSummary getMemberPointSummary(long memberNumber);

  MemberPoint getMemberPoint(long memberNumber);
}
