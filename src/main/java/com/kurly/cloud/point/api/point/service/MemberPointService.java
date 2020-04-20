/*
 * Kurly (주) License
 * 본 파일의 전부 또는 일부는 Kurly(주)의 사유 재산으로 배포 또는 재배포는 Kurly(주)의 허락없이 엄격히 금지합니다.
 * 적용 예외 License 목록에 포함된 코드의 전부 또는 일부는 해당 범위에 한해서 명시된 License를 적용합니다.
 * 저작자: 김민섭
 * 적용 예외 License 목록:
 * 1)
 */

package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
class MemberPointService {

  private final MemberPointRepository memberPointRepository;

  MemberPoint getOrCreateMemberPoint(long memberNumber) {
    Optional<MemberPoint> memberPoint = memberPointRepository.findById(memberNumber);
    return memberPoint.orElseGet(() -> {
      log.debug("회원의 적립금 정보가 존재하지 않아 새로 생성합니다.[{}]", memberNumber);
      return createMemberPoint(memberNumber, 0, 0);
    });
  }

  MemberPoint createMemberPoint(long memberNumber, int freePoint, int cashPoint) {
    return memberPointRepository.save(MemberPoint.builder()
        .memberNumber(memberNumber)
        .cashPoint(cashPoint)
        .freePoint(freePoint)
        .totalPoint(cashPoint + freePoint)
        .updateTime(LocalDateTime.now())
        .build());
  }

  MemberPoint plusFreePoint(long memberNumber, int amount) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);
    memberPoint.plusPoint(amount, 0);
    return memberPointRepository.save(memberPoint);
  }

  MemberPoint plusCashPoint(long memberNumber, int amount) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);

    if (memberPoint.getFreePoint() < 0) {
      //이 회원은 포인트를 빚진 상태
      int repay = Math.min(Math.abs(memberPoint.getFreePoint()), amount);
      amount = amount - repay;
      memberPoint.plusPoint(repay, 0);
    }
    memberPoint.plusPoint(0, amount);
    return memberPointRepository.save(memberPoint);
  }

  MemberPoint minusFreePoint(long memberNumber, int amount) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);
    memberPoint.minusPoint(amount, 0);
    return memberPointRepository.save(memberPoint);
  }

  MemberPoint minusCashPoint(long memberNumber, int amount) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);
    memberPoint.minusPoint(0, amount);
    return memberPointRepository.save(memberPoint);
  }
}
