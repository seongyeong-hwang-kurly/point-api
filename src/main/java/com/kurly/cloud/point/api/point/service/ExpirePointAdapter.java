package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.domain.PointExpireResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.history.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.port.in.ExpirePointPort;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class ExpirePointAdapter implements ExpirePointPort {

  private final MemberPointService memberPointService;
  private final MemberPointHistoryService memberPointHistoryService;
  private final PointService pointService;
  private final PointHistoryService pointHistoryService;

  @Transactional
  @Override public PointExpireResult expireMemberPoint(long memberNumber,
                                                       LocalDateTime expireTime) {
    List<Point> expiredMemberPoint = pointService.getExpiredMemberPoint(memberNumber, expireTime);
    PointExpireResult pointExpireResult = doExpire(expiredMemberPoint);
    pointExpireResult.setMemberNumber(memberNumber);

    memberPointService.minusFreePoint(memberNumber, pointExpireResult.getTotalExpired());

    memberPointHistoryService.insertHistory(MemberPointHistoryInsertRequest.builder()
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
      int remain = point.getRemain();
      long pointSeq = point.getSeq();
      pointExpireResult.add(pointSeq, remain);

      point.expire();

      pointHistoryService.insertHistory(PointHistoryInsertRequest.builder()
          .amount(-remain)
          .historyType(HistoryType.TYPE_103.getValue())
          .detail(HistoryType.TYPE_103.buildMessage())
          .pointSeq(pointSeq)
          .build());
    });

    return pointExpireResult;
  }
}
