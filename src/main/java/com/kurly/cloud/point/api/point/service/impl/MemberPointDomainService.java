package com.kurly.cloud.point.api.point.service.impl;

import com.kurly.cloud.point.api.point.entity.MemberPoint;
import com.kurly.cloud.point.api.point.repository.MemberPointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
class MemberPointDomainService {

  private final MemberPointRepository memberPointRepository;

  MemberPoint getOrCreateMemberPoint(long memberNumber) {
    Optional<MemberPoint> memberPoint = getMemberPoint(memberNumber);
    return memberPoint.orElseGet(() -> {
      log.debug("회원의 적립금 정보가 존재하지 않아 새로 생성합니다.[{}]", memberNumber);
      return createMemberPoint(memberNumber, 0, 0);
    });
  }

  Optional<MemberPoint> getMemberPoint(long memberNumber) {
    return memberPointRepository.findById(memberNumber);
  }

  MemberPoint createMemberPoint(long memberNumber, int freePoint, int cashPoint) {
    return memberPointRepository.saveAndFlush(MemberPoint.builder()
        .memberNumber(memberNumber)
        .cashPoint(cashPoint)
        .freePoint(freePoint)
        .totalPoint(cashPoint + freePoint)
        .updateTime(LocalDateTime.now())
        .build());
  }

  MemberPoint plusFreePoint(long memberNumber, long amount) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);
    memberPoint.plusPoint(amount, 0);
    return memberPointRepository.saveAndFlush(memberPoint);
  }

  MemberPoint plusCashPoint(long memberNumber, long amount) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);

    if (memberPoint.getFreePoint() < 0) {
      //이 회원은 적립금을 빚진 상태
      long repay = Math.min(Math.abs(memberPoint.getFreePoint()), amount);
      amount = amount - repay;
      memberPoint.plusPoint(repay, 0);
    }
    memberPoint.plusPoint(0, amount);
    return memberPointRepository.saveAndFlush(memberPoint);
  }

  MemberPoint minusFreePoint(long memberNumber, long amount) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);
    memberPoint.minusPoint(amount, 0);
    return memberPointRepository.saveAndFlush(memberPoint);
  }

  MemberPoint expireFreePoint(long memberNumber, long amount, LocalDateTime expiredAt) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);
    memberPoint.expire(amount, 0, expiredAt);
    return memberPointRepository.saveAndFlush(memberPoint);
  }

  MemberPoint minusCashPoint(long memberNumber, long amount) {
    MemberPoint memberPoint = getOrCreateMemberPoint(memberNumber);
    memberPoint.minusPoint(0, amount);
    return memberPointRepository.saveAndFlush(memberPoint);
  }
}
