package com.kurly.cloud.point.api.point.service;

import com.kurly.cloud.point.api.point.domain.calculate.ReserveCalculateRequest;
import com.kurly.cloud.point.api.point.domain.calculate.ReserveCalculateResponse;

/**
 * 적립 예정 적립금 계산 유즈케이스
 */
public interface CalculateReserveUseCase {
  /**
   * 적립 예정 적립금을 계산합니다.
   *
   * @param request 적립금 계산 요청
   * @return 계산된 예상 적립금 정보
   */
  ReserveCalculateResponse calculate(ReserveCalculateRequest request);
}
