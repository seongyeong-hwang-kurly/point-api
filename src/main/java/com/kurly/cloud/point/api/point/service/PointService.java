/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

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

  PointConsumeResult consumeOrderPoint(long memberNumber, long orderNumber, int amount) {
    Optional<Point> orderPoint = pointRepository
        .findByMemberNumberAndOrderNumberAndRemainGreaterThan(memberNumber, orderNumber, 0);

    PointConsumeResult pointConsumeResult = new PointConsumeResult(amount);
    if (orderPoint.isPresent()) {
      Point point = orderPoint.get();
      int consume = Math.min(point.getRemain(), amount);
      point.setRemain(point.getRemain() - consume);
      pointConsumeResult.add(point.getSeq(), consume, point.isSettle());
    }

    if (pointConsumeResult.getRemain() > 0) {
      pointConsumeResult.add(consumeMemberPoint(memberNumber, pointConsumeResult.getRemain()));
    }

    return pointConsumeResult;
  }

  PointConsumeResult consumeMemberPoint(long memberNumber, int amount) {
    return this.consumeMemberPoint(memberNumber, amount, false);
  }

  PointConsumeResult consumeMemberPoint(long memberNumber, int amount, boolean onlySettle) {
    List<Point> availablePoints = getAvailableMemberPoint(memberNumber, onlySettle);

    PointConsumeResult pointConsumeResult = new PointConsumeResult(amount);

    for (Point point : availablePoints) {
      if (amount == 0) {
        break;
      }
      int consume = Math.min(point.getRemain(), amount);
      amount = amount - consume;
      point.setRemain(point.getRemain() - consume);
      pointConsumeResult.add(point.getSeq(), consume, point.isSettle());
    }

    return pointConsumeResult;
  }

  List<Point> getAvailableMemberPoint(long memberNumber, boolean onlySettle) {
    return onlySettle ?
        getAvailableSettleMemberPoint(memberNumber) : getAvailableMemberPoint(memberNumber);
  }

  List<Point> getAvailableSettleMemberPoint(long memberNumber) {
    List<Point> availablePoints =
        pointRepository.findAllAvailableSettleMemberPoint(memberNumber, LocalDateTime.now());
    availablePoints.sort(ConsumeOrderComparator.getInstance());
    return availablePoints;
  }

  List<Point> getAvailableMemberPoint(long memberNumber) {
    List<Point> availablePoints =
        pointRepository.findAllAvailableMemberPoint(memberNumber, LocalDateTime.now());
    availablePoints.sort(ConsumeOrderComparator.getInstance());
    return availablePoints;
  }

  PointConsumeResult repayMemberPoint(Long memberNumber, int amount) {
    List<Point> debtMemberPoint = pointRepository.findAllDebtMemberPoint(memberNumber);
    PointConsumeResult pointConsumeResult = new PointConsumeResult(amount);

    for (Point point : debtMemberPoint) {
      if (amount == 0) {
        break;
      }
      int repay = Math.min(Math.abs(point.getRemain()), amount);
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
        .findByOrderNumberAndHistoryType(orderNumber, HistoryType.TYPE_1.getValue());
  }
}
