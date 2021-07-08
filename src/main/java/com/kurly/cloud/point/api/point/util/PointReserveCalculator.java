package com.kurly.cloud.point.api.point.util;

import com.kurly.cloud.point.api.point.domain.reserve.ProductReserveType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.EqualsAndHashCode;

public class PointReserveCalculator {

  @EqualsAndHashCode
  public static class Result {
    ProductReserveType reserveType;
    float reserveValue;
    int reserve;

    public Result(ProductReserveType reserveType, float reserveValue, int reserve) {
      this.reserveType = reserveType;
      this.reserveValue = reserveValue;
      this.reserve = reserve;
    }
  }

  /**
   * 상품의 적립 타입과 회원의 적립률을 이용하여 발생 적립금을 계산한다.
   *
   * @param reserveType             상품 적립금 적립 타입
   * @param productReserveValue     상품 적립금 적립 값 {@link ProductReserveType}
   * @param payPrice                결제 금액
   * @param memberPointReserveRatio 회원 적립률
   * @return 발생 예정 적립금과 정책 정보
   * @see ProductReserveType
   */
  public static Result calculate(ProductReserveType reserveType,
                                 int productReserveValue,
                                 int payPrice,
                                 float memberPointReserveRatio) {

    int calculated;
    ProductReserveType appliedReserveType = reserveType;
    float reserveValue = productReserveValue;

    switch (reserveType) {
      case MEMBER:
        calculated = calculate(payPrice, memberPointReserveRatio);
        reserveValue = memberPointReserveRatio;
        break;
      case FIXED:
        calculated = productReserveValue;
        break;
      case EXCLUDE:
        return new Result(appliedReserveType, 0, 0);
      case PERCENT:
        calculated = calculate(payPrice, productReserveValue);
        break;
      case MAX_PERCENT:
        if (memberPointReserveRatio <= productReserveValue) {
          appliedReserveType = ProductReserveType.MEMBER;
          reserveValue = memberPointReserveRatio;
        }
        calculated = calculate(payPrice, Math.min(memberPointReserveRatio, productReserveValue));
        break;
      case MAX_FIXED:
        int memberPointReserve = calculate(payPrice, memberPointReserveRatio);
        if (memberPointReserve <= productReserveValue) {
          appliedReserveType = ProductReserveType.MEMBER;
          reserveValue = memberPointReserveRatio;
        }
        calculated = Math.min(memberPointReserve, productReserveValue);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + reserveType);
    }

    return new Result(appliedReserveType, reserveValue, Math.max(calculated, 0));
  }

  /**
   * 적립률에 따라 발생 적립금을 계산 한다.
   *
   * @param payPrice 결제금액
   * @param ratio    적립률
   * @return 발생 적립금
   */
  public static int calculate(int payPrice, float ratio) {
    if (ratio == 0 || payPrice == 0) {
      return 0;
    }
    int point = new BigDecimal(payPrice)
        .multiply(new BigDecimal(ratio))
        .divide(new BigDecimal(100), 0, RoundingMode.HALF_UP)
        .intValue();
    return point == 0 ? 1 : point;
  }
}
