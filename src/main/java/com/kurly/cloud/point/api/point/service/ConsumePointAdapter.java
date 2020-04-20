package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.consume.CancelOrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.ConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.OrderConsumePointRequest;
import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.history.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import com.kurly.cloud.point.api.point.exception.CancelAmountExceedException;
import com.kurly.cloud.point.api.point.exception.NotEnoughPointException;
import com.kurly.cloud.point.api.point.service.port.in.ConsumePointPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ConsumePointAdapter implements ConsumePointPort {

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
        .freePoint(-pointConsumeResult.getTotalFreePointConsumed())
        .cashPoint(-pointConsumeResult.getTotalCashPointConsumed())
        .orderNumber(request.getOrderNumber())
        .build());

    return pointConsumeResult;
  }

  @Transactional
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

  /**
   * 현재는 포인트를 사용취소하면 사용취소한 만큼의 금액이 새로 적립됩니다.(유효기간 갱신) <br/>
   * 이 정책은 추후 적립금 개편 시 기존 적립금에 다시 추가 하는 형태로 변경될 예정입니다.
   */
  @Transactional
  @Override public void cancelConsumeByOrder(CancelOrderConsumePointRequest request)
      throws CancelAmountExceedException {
    List<PointHistory> consumedByOrderNumber =
        pointHistoryService.getConsumedByOrderNumber(request.getOrderNumber());

    int totalConsumedAmount = consumedByOrderNumber.stream().mapToInt(PointHistory::getAmount).sum();

    if (request.getPoint() > Math.abs(totalConsumedAmount)) {
      throw new CancelAmountExceedException(request.getOrderNumber(), request.getPoint());
    }

    PublishPointRequest publishRequest = PublishPointRequest.builder()
        .orderNumber(request.getOrderNumber())
        .actionMemberNumber(request.getActionMemberNumber())
        .memberNumber(request.getMemberNumber())
        .point(request.getPoint())
        .historyType(request.getHistoryType())
        .detail(request.getDetail())
        .build();

    Point point = pointService.publishPoint(publishRequest);

    pointHistoryService.insertHistory(PointHistoryInsertRequest.builder()
        .pointSeq(point.getSeq())
        .amount(request.getPoint())
        .historyType(request.getHistoryType())
        .orderNumber(request.getOrderNumber())
        .detail(request.getDetail())
        .actionMemberNumber(request.getActionMemberNumber())
        .build());

    memberPointService.plusFreePoint(request.getMemberNumber(), request.getPoint());

    memberPointHistoryService.insertHistory(MemberPointHistoryInsertRequest
        .builder()
        .memberNumber(request.getMemberNumber())
        .type(request.getHistoryType())
        .freePoint(request.getPoint())
        .expireTime(publishRequest.getExpireDate())
        .detail(request.getDetail())
        .orderNumber(request.getOrderNumber())
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
