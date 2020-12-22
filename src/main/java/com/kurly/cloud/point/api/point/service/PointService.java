package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.consume.ConsumeOrderComparator;
import com.kurly.cloud.point.api.point.domain.consume.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.history.HistoryType;
import com.kurly.cloud.point.api.point.domain.publish.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.repository.PointRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class PointService {

  private final PointRepository pointRepository;

  Point publishPoint(PublishPointRequest request) {
    return pointRepository.save(request.toEntity());
  }

  PointConsumeResult consumeOrderPoint(long memberNumber, long orderNumber, long amount) {
    Optional<Point> orderPoint =
        pointRepository.findAvailableOrderPublishedPoint(memberNumber, orderNumber);

    PointConsumeResult pointConsumeResult = new PointConsumeResult(amount);
    if (orderPoint.isPresent()) {
      Point point = orderPoint.get();
      long consume = Math.min(point.getRemain(), amount);
      point.setRemain(point.getRemain() - consume);
      pointConsumeResult.add(point.getSeq(), consume, point.isSettle());
    }

    if (pointConsumeResult.getRemain() > 0) {
      pointConsumeResult.add(consumeMemberPoint(memberNumber, pointConsumeResult.getRemain()));
    }

    return pointConsumeResult;
  }

  PointConsumeResult consumeMemberPoint(long memberNumber, long amount) {
    return this.consumeMemberPoint(memberNumber, amount, false);
  }

  PointConsumeResult consumeMemberPoint(long memberNumber, long amount, boolean onlySettle) {
    List<Point> availablePoints = getAvailableMemberPoint(memberNumber, onlySettle);

    PointConsumeResult pointConsumeResult = new PointConsumeResult(amount);

    for (Point point : availablePoints) {
      if (amount == 0) {
        break;
      }
      long consume = Math.min(point.getRemain(), amount);
      amount = amount - consume;
      point.setRemain(point.getRemain() - consume);
      pointConsumeResult.add(point.getSeq(), consume, point.isSettle());
    }

    return pointConsumeResult;
  }

  List<Point> getAvailableMemberPoint(long memberNumber, boolean onlySettle) {
    return onlySettle
        ? getAvailableSettleMemberPoint(memberNumber) : getAvailableMemberPoint(memberNumber);
  }

  List<Point> getAvailableMemberPoint(long memberNumber) {
    List<Point> availablePoints =
        pointRepository.findAllAvailableMemberPoint(memberNumber, LocalDateTime.now());
    availablePoints.sort(ConsumeOrderComparator.getInstance());
    return availablePoints;
  }

  List<Point> getAvailableSettleMemberPoint(long memberNumber) {
    List<Point> availablePoints =
        pointRepository.findAllAvailableSettleMemberPoint(memberNumber, LocalDateTime.now());
    availablePoints.sort(ConsumeOrderComparator.getInstance());
    return availablePoints;
  }

  PointConsumeResult repayMemberPoint(Long memberNumber, long amount) {
    List<Point> debtMemberPoint = pointRepository.findAllDebtMemberPoint(memberNumber);
    PointConsumeResult pointConsumeResult = new PointConsumeResult(amount);

    for (Point point : debtMemberPoint) {
      if (amount == 0) {
        break;
      }
      long repay = Math.min(Math.abs(point.getRemain()), amount);
      amount = amount - repay;
      point.setRemain(point.getRemain() + repay);
      pointConsumeResult.add(point.getSeq(), repay, point.isSettle());
    }

    return pointConsumeResult;
  }

  public List<Point> getExpiredMemberPoint(long memberNumber, LocalDateTime expireTime) {
    return pointRepository.findAllExpiredMemberPoint(memberNumber, expireTime);
  }

  public Optional<LocalDateTime> getMemberPointNextExpireDate(long memberNumber) {
    Page<LocalDateTime> memberNextExpireTime =
        pointRepository.getMemberNextExpireTime(memberNumber, PageRequest.of(0, 1));

    if (memberNextExpireTime.hasContent()) {
      return Optional.of(memberNextExpireTime.getContent().get(0));
    } else {
      return Optional.empty();
    }
  }

  public Optional<Point> getPublishedByOrderNumber(long orderNumber) {
    return pointRepository
        .findFirstByOrderNumberAndHistoryType(orderNumber, HistoryType.TYPE_1.getValue());
  }
}
