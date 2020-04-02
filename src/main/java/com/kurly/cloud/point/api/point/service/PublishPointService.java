package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.HistoryType;
import com.kurly.cloud.point.api.point.domain.MemberPointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.PointHistoryInsertRequest;
import com.kurly.cloud.point.api.point.domain.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.entity.MemberPointHistory;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.entity.PointHistory;
import com.kurly.cloud.point.api.point.exception.AlreadyPublishedException;
import com.kurly.cloud.point.api.point.service.port.in.PublishPointPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PublishPointService implements PublishPointPort {

  private final PointService pointService;
  private final PointHistoryService pointHistoryService;
  private final MemberPointService memberPointService;
  private final MemberPointHistoryService memberPointHistoryService;

  @Transactional
  @Override public void publish(PublishPointRequest request) {
    Point point = pointService.publishPoint(request);
    insertPointHistory(request, point.getSeq());
    addMemberPoint(request.getMemberNumber(), request.isSettle(), request.getPoint());
    insertMemberPointHistory(request);
  }

  @Override public void publishByOrder(PublishPointRequest request) throws AlreadyPublishedException {
    long orderNumber = request.getOrderNumber();

    List<PointHistory> published = pointHistoryService.getByOrderNumber(orderNumber);
    if (published.size() > 0) {
      throw new AlreadyPublishedException(orderNumber);
    }
    String msg = HistoryType.TYPE_1.buildMessage(String.valueOf(orderNumber), request.getPointRatio());

    request.setDetail(msg);
    request.setHistoryType(HistoryType.TYPE_1.getValue());

    publish(request);
  }

  private MemberPointHistory insertMemberPointHistory(PublishPointRequest request) {
    MemberPointHistoryInsertRequest memberPointHistoryInsertRequest = MemberPointHistoryInsertRequest
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
        .build();
    return memberPointHistoryService.insertHistory(memberPointHistoryInsertRequest);
  }

  private MemberPoint addMemberPoint(Long memberNumber, boolean settle, Integer point) {
    if (settle) {
      return memberPointService.plusCashPoint(memberNumber, point);
    }
    return memberPointService.plusFreePoint(memberNumber, point);
  }

  private PointHistory insertPointHistory(PublishPointRequest request, long pointSeq) {
    PointHistoryInsertRequest pointHistoryInsertRequest = PointHistoryInsertRequest.builder()
        .pointSeq(pointSeq)
        .amount(request.getPoint())
        .historyType(request.getHistoryType())
        .orderNumber(request.getOrderNumber())
        .settle(request.isSettle())
        .memo(request.getMemo())
        .detail(request.getDetail())
        .actionMemberNumber(request.getActionMemberNumber())
        .build();
    return pointHistoryService.insertHistory(pointHistoryInsertRequest);
  }
  
}
