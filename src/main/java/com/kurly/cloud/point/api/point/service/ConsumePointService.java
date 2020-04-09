package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.service.port.in.ConsumePointPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsumePointService implements ConsumePointPort {

  private final PointService pointService;
  private final PointHistoryService pointHistoryService;
  private final MemberPointService memberPointService;
  private final MemberPointHistoryService memberPointHistoryService;

  @Transactional
  @Override public PointConsumeResult consume(ConsumePointRequest request) throws NotEnoughPointException {
    MemberPoint memberPoint =
        memberPointService.getOrCrateMemberPoint(request.getMemberNumber());

    if (!memberPoint.isEnough(request.getPoint(), request.isSettle())) {
      throw new NotEnoughPointException(request.getPoint(), memberPoint.getTotalPoint());
    }

    PointConsumeResult pointConsumeResult = pointService.consumeMemberPoint(
        request.getMemberNumber(), request.getPoint(), request.isSettle());

    pointConsumeResult.getConsumed().forEach(consumedPoint -> {
      pointHistoryService.insertHistory(PointHistoryInsertRequest.builder()
          .amount(-consumedPoint.getConsumed())
          .historyType(request.getHistoryType())
          .pointSeq(consumedPoint.getPointSeq())
          .detail(request.getDetail())
          .orderNumber(request.getOrderNumber())
          .settle(consumedPoint.isSettle())
          .actionMemberNumber(request.getActionMemberNumber())
          .memo(request.getMemo())
          .build());
    });

    minusMemberPoint(request.getMemberNumber()
        , pointConsumeResult.getTotalFreePointConsumed()
        , pointConsumeResult.getTotalCashPointConsumed());

    memberPointHistoryService.insertHistory(MemberPointHistoryInsertRequest.builder()
        .memberNumber(request.getMemberNumber())
        .type(request.getHistoryType())
        .detail(request.getDetail())
        .memo(request.getMemo())
        .freePoint(pointConsumeResult.getTotalFreePointConsumed())
        .cashPoint(pointConsumeResult.getTotalCashPointConsumed())
        .orderNumber(request.getOrderNumber())
        .build());

    return pointConsumeResult;
  }

  @Override public PointConsumeResult consumeByOrder(OrderConsumePointRequest request)
      throws NotEnoughPointException {
    return consume(ConsumePointRequest.builder()
        .memberNumber(request.getMemberNumber())
        .orderNumber(request.getOrderNumber())
        .point(request.getPoint())
        .actionMemberNumber(request.getMemberNumber())
        .historyType(request.getHistoryType())
        .detail(request.getDetail())
        .build());
  }

  private void minusMemberPoint(Long memberNumber, int freePoint, int cashPoint) {
    if (freePoint > 0) {
      memberPointService.minusFreePoint(memberNumber, freePoint);
    }
    if (cashPoint > 0) {
      memberPointService.minusCashPoint(memberNumber, cashPoint);
    }
  }
}
