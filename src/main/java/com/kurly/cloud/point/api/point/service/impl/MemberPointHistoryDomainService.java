package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.repository.MemberPointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class MemberPointHistoryDomainService {

  private final MemberPointHistoryRepository memberPointHistoryRepository;

  MemberPointHistory insertHistory(MemberPointHistoryInsertRequest request) {
    return memberPointHistoryRepository.save(request.toEntity());
  }

  Page<MemberPointHistory> getHistoryList(MemberPointHistoryListRequest request) {
    return memberPointHistoryRepository.getMemberPointHistories(request);
  }
}
