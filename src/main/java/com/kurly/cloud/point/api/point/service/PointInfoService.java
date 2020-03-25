/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.entity.PointInfo;
import com.kurly.cloud.point.api.point.repository.PointInfoRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointInfoService {

  private final PointInfoRepository pointInfoRepository;

  PointInfo getOrCratePointInfo(long memberNumber) {
    Optional<PointInfo> optionalPointInfo = pointInfoRepository.findById(memberNumber);
    return optionalPointInfo.orElseGet(() -> {
      log.debug("회원의 적립금 정보가 존재하지 않아 새로 생성합니다.[{}]", memberNumber);
      return createPointInfo(memberNumber, 0, 0);
    });
  }

  PointInfo createPointInfo(long memberNumber, int freePoint, int cashPoint) {
    return pointInfoRepository.save(PointInfo.builder()
        .memberNumber(memberNumber)
        .cashPoint(cashPoint)
        .freePoint(freePoint)
        .totalPoint(cashPoint + freePoint)
        .updateTime(LocalDateTime.now())
        .build());
  }

  PointInfo plusFreePoint(long memberNumber, int point) {
    PointInfo pointInfo = getOrCratePointInfo(memberNumber);
    pointInfo.plusPoint(pointInfo, point, 0);
    return pointInfoRepository.save(pointInfo);
  }

  PointInfo plusCashPoint(long memberNumber, int point) {
    PointInfo pointInfo = getOrCratePointInfo(memberNumber);
    pointInfo.plusPoint(pointInfo, 0, point);
    return pointInfoRepository.save(pointInfo);
  }

  PointInfo minusFreePoint(long memberNumber, int point) {
    PointInfo pointInfo = getOrCratePointInfo(memberNumber);
    pointInfo.minusPoint(pointInfo, point, 0);
    return pointInfoRepository.save(pointInfo);
  }

  PointInfo minusCashPoint(long memberNumber, int point) {
    PointInfo pointInfo = getOrCratePointInfo(memberNumber);
    pointInfo.minusPoint(pointInfo, 0, point);
    return pointInfoRepository.save(pointInfo);
  }
}
