/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.ConsumeOrderComparator;
import com.kurly.cloud.point.api.point.domain.PointConsumeResult;
import com.kurly.cloud.point.api.point.domain.PublishPointRequest;
import com.kurly.cloud.point.api.point.entity.Point;
import com.kurly.cloud.point.api.point.repository.PointRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {

  private final PointRepository pointRepository;

  Point publishPoint(PublishPointRequest request) {
    return pointRepository.save(request.toEntity());
  }

  PointConsumeResult consumeMemberPoint(long memberNumber, int amount) {
    List<Point> availableMemberPoint = getAvailableMemberPoint(memberNumber);
    PointConsumeResult pointConsumeResults = new PointConsumeResult(amount);

    for (Point point : availableMemberPoint) {
      if (amount == 0) break;
      int consume = Math.min(point.getRemain(), amount);
      amount = amount - consume;
      point.setRemain(point.getRemain() - consume);
      pointConsumeResults.add(point.getSeq(), consume);
    }

    return pointConsumeResults;
  }

  List<Point> getAvailableMemberPoint(long memberNumber) {
    List<Point> availablePoints =
        pointRepository.findAllAvailableMemberPoint(memberNumber, LocalDateTime.now());
    availablePoints.sort(ConsumeOrderComparator.getInstance());
    return availablePoints;
  }
}
