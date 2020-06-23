package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.adapter.out.dto.MemberPointHistoryDto;
import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.port.out.MemberPointPort;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class MemberPointAdapter implements MemberPointPort {

  final PointService pointService;
  final MemberPointService memberPointService;
  final MemberPointHistoryService memberPointHistoryService;

  @Transactional(readOnly = true)
  @Override public Page<MemberPointHistoryDto> getMemberHistoryList(
      MemberPointHistoryListRequest request) {
    Page<MemberPointHistory> pointHistories = memberPointHistoryService.getHistoryList(request);
    return pointHistories.map(memberPointHistory
        -> MemberPointHistoryDto.fromEntity(memberPointHistory, request.isIncludeMemo()));
  }

  @Transactional(readOnly = true)
  @Override public MemberPointSummary getMemberPointSummary(long memberNumber) {
    int totalPoint = memberPointService.getMemberPoint(memberNumber)
        .orElseGet(MemberPoint::new).getTotalPoint();
    if (totalPoint == 0) {
      return MemberPointSummary.byEmptyExpireAmount(0);
    }

    Optional<LocalDateTime> memberPointNextExpireDate =
        pointService.getMemberPointNextExpireDate(memberNumber);
    LocalDateTime nextSystemExpireDate =
        PointExpireDateCalculator.calculateNext(LocalDateTime.now());

    if (memberPointNextExpireDate.isEmpty()
        || nextSystemExpireDate.isBefore(memberPointNextExpireDate.get())) {
      return MemberPointSummary.byEmptyExpireAmount(totalPoint);
    }

    List<Point> expirePoints =
        pointService.getExpiredMemberPoint(memberNumber, memberPointNextExpireDate.get());

    int expireAmount = expirePoints.stream().mapToInt(Point::getRemain).sum();

    return MemberPointSummary.builder()
        .amount(totalPoint)
        .nextExpireAmount(expireAmount)
        .nextExpireDate(memberPointNextExpireDate.get())
        .build();
  }


}
