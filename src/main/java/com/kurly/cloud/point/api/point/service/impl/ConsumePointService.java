package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
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
import com.kurly.cloud.point.api.point.service.ConsumePointUseCase;
import com.kurly.cloud.point.api.point.util.DateTimeUtil;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ConsumePointService implements ConsumePointUseCase {

  private final PointDomainService pointDomainService;
  private final PointHistoryDomainService pointHistoryDomainService;
  private final MemberPointDomainService memberPointDomainService;
  private final MemberPointHistoryDomainService memberPointHistoryDomainService;

  @Retryable(
      value = OptimisticLockingFailureException.class,
      maxAttempts = 5,
      backoff = @Backoff(delay = 100, random = true)
  )
  @Transactional
  @Override
  public PointConsumeResult consume(ConsumePointRequest request)
      throws NotEnoughPointException {
    MemberPoint memberPoint =
        memberPointDomainService.getOrCreateMemberPoint(request.getMemberNumber());

    if (!memberPoint.isEnough(request.getPoint(), request.isSettle())) {
      throw new NotEnoughPointException(request.getPoint(), memberPoint.getTotalPoint());
    }

    PointConsumeResult pointConsumeResult = pointDomainService.consumeMemberPoint(
        request.getMemberNumber(), request.getPoint(), request.isSettle());

    pointConsumeResult.getConsumed().forEach(consumedPoint -> {
      pointHistoryDomainService.insertHistory(PointHistoryInsertRequest.builder()
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

    minusMemberPoint(request.getMemberNumber(),
        pointConsumeResult.getTotalFreePointConsumed(),
        pointConsumeResult.getTotalCashPointConsumed());

    memberPointHistoryDomainService.insertHistory(MemberPointHistoryInsertRequest.builder()
        .memberNumber(request.getMemberNumber())
        .type(request.getHistoryType())
        .detail(request.getDetail())
        .memo(request.getMemo())
        .freePoint(-pointConsumeResult.getTotalFreePointConsumed())
        .cashPoint(-pointConsumeResult.getTotalCashPointConsumed())
        .orderNumber(request.getOrderNumber())
        .build());

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "pointConsumed");
        put("memberNumber", request.getMemberNumber());
        put("amount", request.getPoint());
        put("type", request.getHistoryType());
        put("orderNumber", request.getOrderNumber());
        put("actionMemberNumber", request.getActionMemberNumber());
        put("detail", request.getDetail());
      }
    });

    return pointConsumeResult;
  }

  @Transactional
  @Override
  public PointConsumeResult consumeByOrder(OrderConsumePointRequest request)
      throws NotEnoughPointException {
    return consume(ConsumePointRequest.builder()
        .memberNumber(request.getMemberNumber())
        .orderNumber(request.getOrderNumber())
        .point(request.getPoint())
        .actionMemberNumber(request.getMemberNumber())
        .historyType(request.getHistoryType())
        .settle(request.isSettle())
        .detail(request.getDetail())
        .build());
  }

  /**
   * 현재는 적립금을 사용취소하면 사용취소한 만큼의 금액이 새로 적립됩니다.(유효기간 갱신) <br/>
   * 이 정책은 추후 적립금 개편 시 기존 적립금에 다시 추가 하는 형태로 변경될 예정입니다.
   */
  @Retryable(
      value = OptimisticLockingFailureException.class,
      maxAttempts = 5,
      backoff = @Backoff(delay = 100, random = true)
  )
  @Transactional
  @Override
  public void cancelConsumeByOrder(CancelOrderConsumePointRequest request)
      throws CancelAmountExceedException {
    List<PointHistory> consumedByOrderNumber =
        pointHistoryDomainService.getConsumedByOrderNumber(request.getOrderNumber());

    long totalConsumedAmount =
        consumedByOrderNumber.stream().mapToLong(PointHistory::getAmount).sum();

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

    Point point = pointDomainService.publishPoint(publishRequest);

    pointHistoryDomainService.insertHistory(PointHistoryInsertRequest.builder()
        .pointSeq(point.getSeq())
        .amount(request.getPoint())
        .historyType(request.getHistoryType())
        .orderNumber(request.getOrderNumber())
        .detail(request.getDetail())
        .actionMemberNumber(request.getActionMemberNumber())
        .build());

    memberPointDomainService.plusFreePoint(request.getMemberNumber(), request.getPoint());

    memberPointHistoryDomainService.insertHistory(MemberPointHistoryInsertRequest
        .builder()
        .memberNumber(request.getMemberNumber())
        .type(request.getHistoryType())
        .freePoint(request.getPoint())
        .expireTime(DateTimeUtil.toLocalDateTime(publishRequest.getExpireDate()))
        .detail(request.getDetail())
        .orderNumber(request.getOrderNumber())
        .build());

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "pointConsumeCanceled");
        put("memberNumber", request.getMemberNumber());
        put("amount", request.getPoint());
        put("type", request.getHistoryType());
        put("orderNumber", request.getOrderNumber());
        put("actionMemberNumber", request.getActionMemberNumber());
        put("detail", request.getDetail());
      }
    });
  }

  private void minusMemberPoint(Long memberNumber, long freePoint, long cashPoint) {
    if (freePoint > 0) {
      memberPointDomainService.minusFreePoint(memberNumber, freePoint);
    }
    if (cashPoint > 0) {
      memberPointDomainService.minusCashPoint(memberNumber, cashPoint);
    }
  }
}
