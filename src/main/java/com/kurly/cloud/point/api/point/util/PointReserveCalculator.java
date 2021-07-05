package com.kurly.cloud.point.api.point.util;

import com.kurly.cloud.point.api.point.domain.reserve.ProductReserveType;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class PointReserveCalculator {

  /**
   * 상품의 적립 타입과 회원의 적립률을 이용하여 발생 적립금을 계산한다.
   *
   * @param reserveType             상품 적립금 적립 타입
   * @param productReserveValue     상품 적립금 적립 값 {@link ProductReserveType}
   * @param payPrice                결제 금액
   * @param memberPointReserveRatio 회원 적립률
   * @return 발생 적립금
   * @see ProductReserveType
   */
  public static int calculate(ProductReserveType reserveType,
                              int productReserveValue,
                              int payPrice,
                              float memberPointReserveRatio) {

    int calculated;

    switch (reserveType) {
      case MEMBER:
        calculated = calculate(payPrice, memberPointReserveRatio);
        break;
      case FIXED:
        calculated = productReserveValue;
        break;
      case EXCLUDE:
        return 0;
      case PERCENT:
        calculated = calculate(payPrice, productReserveValue);
        break;
      case MAX_PERCENT:
        calculated = calculate(payPrice, Math.min(memberPointReserveRatio, productReserveValue));
        break;
      case MAX_FIXED:
        calculated = Math.min(
            calculate(payPrice, memberPointReserveRatio),
            productReserveValue
        );
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + reserveType);
    }

    return Math.max(calculated, 0);
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
