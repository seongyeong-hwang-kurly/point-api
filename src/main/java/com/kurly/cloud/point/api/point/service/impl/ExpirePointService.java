package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.domain.PointExpireResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.history.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.service.ExpirePointUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class ExpirePointService implements ExpirePointUseCase {

  private final MemberPointDomainService memberPointDomainService;
  private final MemberPointHistoryDomainService memberPointHistoryDomainService;
  private final PointDomainService pointDomainService;
  private final PointHistoryDomainService pointHistoryDomainService;

  @Transactional
  @Override
  public PointExpireResult expireMemberPoint(long memberNumber,
                                             LocalDateTime expireTime) {
    List<Point> expiredMemberPoint = pointDomainService
        .getExpiredMemberPoint(memberNumber, expireTime);
    PointExpireResult pointExpireResult = doExpire(expiredMemberPoint);
    pointExpireResult.setMemberNumber(memberNumber);

    memberPointDomainService.minusFreePoint(memberNumber, pointExpireResult.getTotalExpired());

    memberPointHistoryDomainService.insertHistory(MemberPointHistoryInsertRequest.builder()
        .detail(HistoryType.TYPE_103.buildMessage())
        .freePoint(-pointExpireResult.getTotalExpired())
        .memberNumber(memberNumber)
        .type(HistoryType.TYPE_103.getValue())
        .build());

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "pointExpired");
        put("memberNumber", pointExpireResult.getMemberNumber());
        put("expired", pointExpireResult.getTotalExpired());
        put("expiredPointSeq", pointExpireResult.getExpiredPointSeq());
      }
    });

    return pointExpireResult;
  }

  private PointExpireResult doExpire(List<Point> points) {
    PointExpireResult pointExpireResult = new PointExpireResult();

    points.forEach(point -> {
      long remain = point.getRemain();
      long pointSeq = point.getSeq();
      pointExpireResult.add(pointSeq, remain);

      point.expire();

      pointHistoryDomainService.insertHistory(PointHistoryInsertRequest.builder()
          .amount(-remain)
          .historyType(HistoryType.TYPE_103.getValue())
          .detail(HistoryType.TYPE_103.buildMessage())
          .expireTime(point.getExpireTime())
          .pointSeq(pointSeq)
          .build());
    });

    return pointExpireResult;
  }
}
