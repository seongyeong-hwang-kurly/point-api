package com.kurly.cloud.point.api.point.repository;

import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberPointHistoryCustomRepository {
  Page<MemberPointHistory> getMemberPointHistories(MemberPointHistoryListRequest request);
}
