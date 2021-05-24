package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.domain.MemberPointSummary;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryListRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.service.MemberPointUseCase;
import com.kurly.cloud.point.api.point.util.PointExpireDateCalculator;
import com.kurly.cloud.point.api.point.web.dto.MemberPointHistoryDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class MemberPointService implements MemberPointUseCase {

  final PointDomainService pointDomainService;
  final MemberPointDomainService memberPointDomainService;
  final MemberPointHistoryDomainService memberPointHistoryDomainService;

  @Transactional(readOnly = true)
  @Override
  public Page<MemberPointHistoryDto> getMemberHistoryList(
      MemberPointHistoryListRequest request) {
    Page<MemberPointHistory> pointHistories =
        memberPointHistoryDomainService.getHistoryList(request);
    return pointHistories.map(memberPointHistory
        -> MemberPointHistoryDto.fromEntity(memberPointHistory, request.isIncludeMemo()));
  }

  @Transactional(readOnly = true)
  @Override
  public MemberPointSummary getMemberPointSummary(long memberNumber) {
    long totalPoint = memberPointDomainService.getMemberPoint(memberNumber)
        .orElseGet(MemberPoint::new).getTotalPoint();
    if (totalPoint == 0) {
      return MemberPointSummary.byEmptyExpireAmount(0);
    }

    Optional<LocalDateTime> memberPointNextExpireDate =
        pointDomainService.getMemberPointNextExpireDate(memberNumber);
    LocalDateTime nextSystemExpireDate =
        PointExpireDateCalculator.calculateNext(LocalDateTime.now());

    if (memberPointNextExpireDate.isEmpty()
        || nextSystemExpireDate.isBefore(memberPointNextExpireDate.get())) {
      return MemberPointSummary.byEmptyExpireAmount(totalPoint);
    }

    List<Point> expirePoints =
        pointDomainService.getExpiredMemberPoint(memberNumber, memberPointNextExpireDate.get());

    long expireAmount = expirePoints.stream().mapToLong(Point::getRemain).sum();

    return MemberPointSummary.builder()
        .amount(totalPoint)
        .nextExpireAmount(expireAmount)
        .nextExpireDate(memberPointNextExpireDate.get())
        .build();
  }

  @Transactional(readOnly = true)
  @Override
  public MemberPoint getMemberPoint(long memberNumber) {
    return memberPointDomainService.getMemberPoint(memberNumber)
        .orElse(MemberPoint.builder()
            .memberNumber(memberNumber)
            .totalPoint(0)
            .freePoint(0)
            .cashPoint(0)
            .build());
  }


}
