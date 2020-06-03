package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.api.common.util.logging.FileBeatLogger;
import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.history.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.history.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.publish.CancelPublishOrderPointRequest;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.port.in.PublishPointPort;
import java.util.HashMap;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PublishPointAdapter implements PublishPointPort {

  private final PointService pointService;
  private final PointHistoryService pointHistoryService;
  private final MemberPointService memberPointService;
  private final MemberPointHistoryService memberPointHistoryService;

  @Transactional
  @Override public Point publish(PublishPointRequest request) {
    Point point = pointService.publishPoint(request);

    pointHistoryService.insertHistory(PointHistoryInsertRequest.builder()
        .pointSeq(point.getSeq())
        .amount(request.getPoint())
        .historyType(request.getHistoryType())
        .orderNumber(request.getOrderNumber())
        .settle(request.isSettle())
        .memo(request.getMemo())
        .detail(request.getDetail())
        .actionMemberNumber(request.getActionMemberNumber())
        .build());

    MemberPoint memberPoint =
        plusMemberPoint(request.getMemberNumber(), request.isSettle(), request.getPoint());

    memberPointHistoryService.insertHistory(MemberPointHistoryInsertRequest
        .builder()
        .memberNumber(request.getMemberNumber())
        .type(request.getHistoryType())
        .cashPoint(request.isSettle() ? request.getPoint() : 0)
        .freePoint(!request.isSettle() ? request.getPoint() : 0)
        .expireTime(request.getExpireDate())
        .hidden(request.isHidden())
        .detail(request.getDetail())
        .memo(request.getMemo())
        .orderNumber(request.getOrderNumber())
        .build());

    if (memberPoint.getRepayAmount(request.getPoint()) > 0) {
      repayPoint(request.getMemberNumber(), memberPoint.getRepayAmount(request.getPoint()));
    }

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "pointPublished");
        put("memberNumber", request.getMemberNumber());
        put("amount", request.getPoint());
        put("type", request.getHistoryType());
        put("orderNumber", request.getOrderNumber());
      }
    });

    return point;
  }

  @Transactional
  @Override public Point publishByOrder(PublishPointRequest request)
      throws AlreadyPublishedException {
    long orderNumber = request.getOrderNumber();

    Optional<Point> published = pointService.getPublishedByOrderNumber(orderNumber);
    if (published.isPresent()) {
      throw new AlreadyPublishedException(orderNumber);
    }
    String msg =
        HistoryType.TYPE_1.buildMessage(String.valueOf(orderNumber), request.getPointRatio());

    request.setDetail(msg);
    request.setHistoryType(HistoryType.TYPE_1.getValue());

    return publish(request);
  }

  @Transactional
  @Override public void cancelPublishByOrder(CancelPublishOrderPointRequest request) {
    PointConsumeResult pointConsumeResult = pointService.consumeOrderPoint(
        request.getMemberNumber(), request.getOrderNumber(), request.getPoint());

    pointConsumeResult.getConsumed().forEach(consumed -> {
      pointHistoryService.insertHistory(PointHistoryInsertRequest.builder()
          .actionMemberNumber(request.getActionMemberNumber())
          .detail(request.getDetail())
          .settle(consumed.isSettle())
          .orderNumber(request.getOrderNumber())
          .historyType(request.getHistoryType())
          .pointSeq(consumed.getPointSeq())
          .amount(-consumed.getConsumed())
          .build()
      );
      minusMemberPoint(request.getMemberNumber(), consumed.isSettle(), consumed.getConsumed());
    });

    //회원이 가진 적립금이 모자른 경우 적립금을 빌려온다 (컬리에게 빚을 짐)
    if (pointConsumeResult.getRemain() > 0) {
      loanPoint(request, pointConsumeResult.getRemain());
    }

    memberPointHistoryService.insertHistory(MemberPointHistoryInsertRequest
        .builder()
        .memberNumber(request.getMemberNumber())
        .type(request.getHistoryType())
        .freePoint(-request.getPoint())
        .expireTime(null)
        .detail(request.getDetail())
        .orderNumber(request.getOrderNumber())
        .build());

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "pointPublishCanceled");
        put("memberNumber", request.getMemberNumber());
        put("amount", request.getPoint());
        put("type", request.getHistoryType());
        put("orderNumber", request.getOrderNumber());
      }
    });

  }

  /**
   * 적립금을 대출합니다.
   *
   * @param request request
   */
  private void loanPoint(CancelPublishOrderPointRequest request, int amount) {
    Point point = pointService.publishPoint(PublishPointRequest.builder()
        .point(-amount)
        .memberNumber(request.getMemberNumber())
        .historyType(HistoryType.TYPE_102.getValue())
        .detail(request.getDetail())
        .orderNumber(request.getOrderNumber())
        .actionMemberNumber(request.getActionMemberNumber())
        .unlimitedDate(true)
        .build());

    pointHistoryService.insertHistory(PointHistoryInsertRequest.builder()
        .actionMemberNumber(request.getActionMemberNumber())
        .detail(request.getDetail())
        .orderNumber(request.getOrderNumber())
        .historyType(HistoryType.TYPE_102.getValue())
        .pointSeq(point.getSeq())
        .amount(-amount)
        .build()
    );

    minusMemberPoint(request.getMemberNumber(), false, amount);

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "pointLoaned");
        put("memberNumber", request.getMemberNumber());
        put("amount", amount);
        put("type", request.getHistoryType());
        put("orderNumber", request.getOrderNumber());
      }
    });
  }

  /**
   * 대출한 적립금을 상환처리 합니다.
   */
  private void repayPoint(Long memberNumber, int amount) {
    // 기 지급 된 적립금에서 상환해야 할 적립금을 가져옴
    PointConsumeResult pointConsumeResult = pointService.consumeMemberPoint(memberNumber, amount);

    pointConsumeResult.getConsumed().forEach(consumedPoint -> {
      pointHistoryService.insertHistory(PointHistoryInsertRequest.builder()
          .amount(-consumedPoint.getConsumed())
          .pointSeq(consumedPoint.getPointSeq())
          .historyType(HistoryType.TYPE_105.getValue())
          .settle(consumedPoint.isSettle())
          .detail(HistoryType.TYPE_105.buildMessage())
          .build());
    });

    memberPointHistoryService.insertHistory(MemberPointHistoryInsertRequest.builder()
        .freePoint(-amount)
        .detail(HistoryType.TYPE_105.buildMessage())
        .type(HistoryType.TYPE_105.getValue())
        .memberNumber(memberNumber)
        .hidden(true)
        .build());

    // 가져온 적립금만큼 빚진 적립금을 상환함
    PointConsumeResult pointRepayResult =
        pointService.repayMemberPoint(memberNumber, pointConsumeResult.getTotalConsumed());

    pointRepayResult.getConsumed().forEach(consumedPoint -> {
      pointHistoryService.insertHistory(PointHistoryInsertRequest.builder()
          .amount(consumedPoint.getConsumed())
          .pointSeq(consumedPoint.getPointSeq())
          .historyType(HistoryType.TYPE_50.getValue())
          .settle(consumedPoint.isSettle())
          .detail(HistoryType.TYPE_50.buildMessage())
          .build());
    });

    FileBeatLogger.info(new HashMap<>() {
      {
        put("action", "pointRepaid");
        put("memberNumber", memberNumber);
        put("amount", amount);
      }
    });
  }

  private MemberPoint plusMemberPoint(Long memberNumber, boolean settle, Integer point) {
    if (settle) {
      return memberPointService.plusCashPoint(memberNumber, point);
    }
    return memberPointService.plusFreePoint(memberNumber, point);
  }

  private MemberPoint minusMemberPoint(Long memberNumber, boolean settle, Integer point) {
    if (settle) {
      return memberPointService.minusCashPoint(memberNumber, point);
    }
    return memberPointService.minusFreePoint(memberNumber, point);
  }

}
