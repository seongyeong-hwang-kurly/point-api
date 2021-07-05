package com.kurly.cloud.point.api.point.domain.reserve;

/**
 * 상품의 적립금 적립 정책 타입.
 * 상품에는 타입과 값으로 적립 정책을 구분한다.
 * 타입 별 값의 의미는 아래와 같음
 *
 * <p>
 * MEMBER(0) : 의미없음
 * FIXED(1) : 고정 적립금
 * EXCLUDE(2) : 의미없음
 * PERCENT(3) : 적립률금
 * MAX_PERCENT(4) : 최대 적립률
 * MAX_FIXED(5) : 최대 고정 적립
 * </p>
 */
public enum ProductReserveType {
  MEMBER,       // 기본정책(회원적립)을 따름
  FIXED,        // 고정 값 ,
  EXCLUDE,      // 적립 제외
  PERCENT,      // 고정 비율
  MAX_PERCENT,  // 최대 비율
  MAX_FIXED     // 최대 고정 값
}
